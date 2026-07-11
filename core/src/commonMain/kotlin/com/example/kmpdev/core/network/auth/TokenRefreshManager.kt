package com.example.kmpdev.core.network.auth


import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TokenRefreshManager(
    private val tokenManager: TokenManager,   // SecureStorage 实现
    private val tokenRefresher: TokenRefresher      // 通过接口进行绑定实现类，在user层进行注入
) {
    private val refreshMutex = Mutex()

    /** 线程安全地获取有效 Token。必要时自动刷新，多个并发请求共享一次刷新结果
     *  401 重试时清除了AccessToken，
     *    如果上个协程还在请求则阻塞，
     *    如果上个线程刷新完，在获取lock后重新确认一下AccessToken是否存在，存在则返回了
     *  清除token的  2中状态
     *   1. 网络请求失败了、
     * */
    suspend fun getValidAccessToken(): String? {
        val currentToken = tokenManager.getAccessToken()
        if (currentToken != null) return currentToken  // fast path

        refreshMutex.withLock {
            val currentToken = tokenManager.getAccessToken()
            if (currentToken != null) return currentToken  // fast path

            return refreshMutex.withLock {
                // ★ 双重检查始终执行：另一个并发 401 可能已刷新完成并保存了新 token
                val doubleCheck = tokenManager.getAccessToken()
                if (doubleCheck != null) return@withLock doubleCheck

                // 执行刷新,refreshToken不存在只能进行重新登录
                val refreshToken = tokenManager.getRefreshToken() ?: run {
                    tokenManager.clearTokens()
                    return@withLock null
                }

                try {
                    val result = tokenRefresher.refresh(refreshToken)
                    tokenManager.saveTokens(result.accessToken, result.refreshToken)
                    result.accessToken
                } catch (e: Exception) {
                    // 1. 刷新失败 → 清除accessToken，可能是网络原因，下次再次重试，
                    // 2. 根据服务端返回的刷新结果进行判断refreshToken是否过期
                    tokenManager.clearAccessToken()
                    null
                }
            }
        }
    }
}