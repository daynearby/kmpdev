package com.example.kmpdev.core.network

import com.example.kmpdev.core.cache.CacheManager
import com.example.kmpdev.core.cache.CachePolicy
import com.example.kmpdev.core.cache.CacheWriteConfig
import com.example.kmpdev.core.logger.Logger
import com.example.kmpdev.core.network.auth.AuthException
import com.example.kmpdev.core.network.auth.AuthRetryHandler
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json


/**
 *
 *  policy == NO_CACHE
 *    fetcher() → 成功: FromNetwork / 失败: classifyError → Error
 *
 *  policy == CACHE_THEN_NETWORK
 *    1. 查缓存（cacheManager.getRawJson）
 *      命中 → emit FromCache              ← UI 立即展示缓存数据
 *      未命中 → 不 emit
 *
 *    2. 检查 inFlight[key]
 *       已有请求正在请求 → 跳过
 *
 *    3. CacheWriteConfig.wrap { fetcher() }   ← 协程上下文注入缓存参数
 *
 *       Ktorfit → HttpClient → HttpCachePlugin.onResponse
 *            通过协程上下文获取对应 CacheWriteConfig，进而获取cacheKey、缓存时长
 *            body<ByteArray> → cacheManager.putRawJson  （透明写缓存）
 *
 *       成功 → emit FromNetwork
 *       失败 → classifyError → NetworkErrorWithCache / Error
 *
 *    4. 清除 inFlight[key]-请求结束清楚状态，下次可以发起
 *
 */
expect fun isNetworkIOException(e: Exception): Boolean


class CacheThenNetworkStrategy(
    val cacheManager: CacheManager,
    val authRetryHandler: AuthRetryHandler,
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
) {
    /** ★ KMP 兼容：用 Mutex+MutableMap 替代 ConcurrentHashMap+AtomicBoolean */
    val inFlight = mutableMapOf<String, Boolean>()
    val inFlightMutex = Mutex()

    /**
     * @param cacheKey 缓存 Key（UseCase 根据 API 路径+参数生成）
     * @param fetcher  Ktorfit 挂起方法（正常返回对象，不返回 HttpResponse）
     */
    suspend inline fun <reified T> execute(
        policy: CachePolicy,
        cacheKey: String = "",
        ttlSeconds: Long = 300,
        noinline fetcher: suspend () -> T
    ): Flow<RequestResult<T>> = flow {

        // ═══ NO_CACHE ═══
        if (policy == CachePolicy.NO_CACHE) {
            try {
                val data = authRetryHandler.execute { fetcher() }  //  不走缓存需要 401 （token刷新）
                emit(RequestResult.FromNetwork(data))
            } catch (e: Exception) {
                if (e is AuthException) return@flow
                emit(RequestResult.Error(classifyError(e)))
            }
            return@flow
        }

        // ═══ CACHE_THEN_NETWORK ═══
        var hasCache = false

        // Step 1: 查缓存
        val rawJson = cacheManager.getRawJson(cacheKey)
        if (rawJson != null) {
            hasCache = true
            try {
                emit(RequestResult.FromCache(json.decodeFromString<T>(rawJson)))
            } catch (_: Exception) {
                cacheManager.remove(cacheKey)
            }
        }

        // Step 2: inFlight（Mutex 保护，跨平台线程安全）
        val isDuplicate = inFlightMutex.withLock {
            if (inFlight[cacheKey] == true) {
                // 上一次请求未结束
                true
            } else {
                // false情况，cacheKey对应值不存在，可以发起新的请求
                inFlight[cacheKey] = true
                false
            }
        }
        if (isDuplicate) {
            Logger.d("CacheThenNetwork", "Duplicate skipped: $cacheKey")
            return@flow
        }

        // Step 3: 网络请求（HttpCachePlugin 透明写缓存）
        try {
            val data = CacheWriteConfig(cacheKey, ttlSeconds).wrap {
                authRetryHandler.execute { fetcher() }   // ★ 401→刷新→重试原始请求
            }
            emit(RequestResult.FromNetwork(data))
        } catch (e: Exception) {

            if (e is AuthException) {
                // 已经显示了登录页面，不需要再显示
                return@flow
            }
            val networkError = classifyError(e)
            if (hasCache) {
                emit(RequestResult.NetworkErrorWithCache(networkError, "网络异常，显示的是上次数据"))
            } else {
                emit(RequestResult.Error(networkError))
            }
        } finally {
            inFlightMutex.withLock {
                inFlight.remove(cacheKey)
            }
        }
    }

    fun classifyError(e: Exception): NetworkError = when (e) {
        is ClientRequestException ->
            NetworkError.HttpError(e.response.status.value, e.message)

        is ServerResponseException ->
            NetworkError.HttpError(e.response.status.value, e.message)

        is HttpRequestTimeoutException, is ConnectTimeoutException, is SocketTimeoutException ->
            NetworkError.NoConnectivity(e)

        is kotlinx.serialization.SerializationException ->
            NetworkError.Serialization(e)

        else -> {
            if (isNetworkIOException(e)) NetworkError.NoConnectivity(e)
            else NetworkError.Unknown(e)
        }
    }
}