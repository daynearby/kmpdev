package com.example.kmpdev.core.network.auth

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

class AuthRetryHandler(
    private val tokenRefreshManager: TokenRefreshManager,
    private val authCoordinator: AuthCoordinator,
    private val maxRetries: Int = 1
) {
    suspend fun <T> execute(request: suspend () -> T): T {
        var retryCount = 0
        while (true) {
            try {
                return request()
            } catch (e: ClientRequestException) {
                if (e.response.status == HttpStatusCode.Unauthorized && retryCount < maxRetries) {
                    retryCount++
                    val newToken = tokenRefreshManager.getValidAccessToken()
                    if (newToken != null) {
                        continue
                    } else {
                        authCoordinator.notifyAuthExpired()  // 全局单次通知
                        throw AuthException("Token refresh failed")
                    }
                }
                throw e
            }
        }
    }
}

class AuthException(message: String) : Exception(message)