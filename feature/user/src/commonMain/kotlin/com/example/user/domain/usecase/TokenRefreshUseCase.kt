package com.example.user.domain.usecase

import com.example.kmpdev.core.network.auth.AuthToken
import com.example.kmpdev.core.network.auth.TokenRefresher
import com.example.user.domain.repository.TokenRefreshRepository

/**
 * 执行token刷新的实际接口
 */
class TokenRefreshUseCase(
    val tokenRefreshRepository: TokenRefreshRepository
) : TokenRefresher {

    override suspend fun refresh(refreshToken: String): AuthToken {
        val authToken = tokenRefreshRepository.refreshToken(refreshToken)
        return authToken.data
    }
}