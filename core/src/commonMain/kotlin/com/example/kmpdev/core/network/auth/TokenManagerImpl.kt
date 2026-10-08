package com.example.kmpdev.core.network.auth

import com.example.kmpdev.core.cache.SecureStorage

class TokenManagerImpl(
    private val secureStorage: SecureStorage
) : TokenManager {

    companion object {
        private const val KEY_ACCESS = "auth_access_token"
        private const val KEY_REFRESH = "auth_refresh_token"
    }

    override suspend fun getAccessToken(): String? =
        secureStorage.getString(KEY_ACCESS)

    override suspend fun getRefreshToken(): String? =
        secureStorage.getString(KEY_REFRESH)

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        secureStorage.setString(KEY_ACCESS, accessToken)
        secureStorage.setString(KEY_REFRESH, refreshToken)
    }

    override suspend fun clearAccessToken() {
        secureStorage.remove(KEY_ACCESS)
    }

    override suspend fun clearTokens() {
        secureStorage.remove(KEY_ACCESS)
        secureStorage.remove(KEY_REFRESH)
    }

}