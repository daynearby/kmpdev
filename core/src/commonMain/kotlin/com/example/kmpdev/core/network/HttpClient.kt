package com.example.kmpdev.core.network

import com.example.kmpdev.core.app.RequestParams
import com.example.kmpdev.core.cache.CacheManager
import com.example.kmpdev.core.cache.CacheWriteConfig
import com.example.kmpdev.core.logger.Logger
import com.example.kmpdev.core.logger.isDebugBuild
import com.example.kmpdev.core.network.auth.TokenManager
import com.example.kmpdev.core.network.interceptors.HttpAuthInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.logging.Logger as KtorLogger

expect fun platformHttpEngine(): HttpClientEngine

/**
 * 在http客户端增加烂机器或者是参数
 */
fun createHttpClient(cacheManager: CacheManager, tokenManager: TokenManager, requestParams: RequestParams): HttpClient {
    val client = HttpClient(platformHttpEngine()) {
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
        install(createHttpCachePlugin(cacheManager))
        install(HttpAuthInterceptor) {
            tokenProvider = { tokenManager.getAccessToken() }
            headerProvider = { requestParams }
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true       // Ktorfit 建议开启，减少空值异常
            })
        }
        //  请求失败重试机制
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)           // 5xx → 重试 3 次
            retryOnException(maxRetries = 3)              // 超时/连接异常 → 重试
            exponentialDelay()                            // 指数退避
            delayMillis { retryCount ->
                (1L shl retryCount.coerceAtMost(3)) * 1000 // 幂等 1s→2s→4s max 8s
            }
        }
        // log信息一般只在debug模式才开启
        if (isDebugBuild()) {
            install(Logging) {
                logger = object : KtorLogger {
                    override fun log(message: String) {
                        Logger.d("Ktor", message)
                    }
                }
                level = LogLevel.BODY
            }
        }
    }
    return client
}

/**
 * 根据配置的
 * 网络请求的响应结果进行缓存
 */
fun createHttpCachePlugin(cacheManager: CacheManager) = createClientPlugin("HttpCachePlugin") {
    onResponse { response ->
        // 从协程上下文读取缓存参数（策略层通过 CacheWriteConfig.wrap 注入）
        // CachePolicy.NO_CACHE则config为空
        val config = currentCoroutineContext()[CacheWriteConfig.Key]
        if (config != null && response.status.isSuccess()) {
            // 读 body 字节 → 原样存缓存 → Ktorfit 仍能正常反序列化
            val bodyBytes = response.body<ByteArray>()
            cacheManager.putRawJson(
                key = config.cacheKey,
                rawJson = bodyBytes.decodeToString(),
                ttlSeconds = config.ttlSeconds
            )
        }
    }
}
