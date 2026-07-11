package com.example.kmpdev.core.network.auth

/**
 * token刷新、存储、清楚
 */
interface TokenManager {
    suspend  fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun clearAccessToken()       // ★ 401 时清除过期 token，触发强制刷新

    suspend fun clearTokens()
}
