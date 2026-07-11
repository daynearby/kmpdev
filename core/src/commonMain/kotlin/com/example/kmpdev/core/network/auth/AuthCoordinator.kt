package com.example.kmpdev.core.network.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 根据刷新token的结果，如果刷新失败会清除本地的token信息，
 * 这种情况就需要通知用户进行登录
 */
class AuthCoordinator(
    private val tokenManager: TokenManager
) {
    private val _loginRequired = MutableStateFlow(false)
    val loginRequired: StateFlow<Boolean> = _loginRequired.asStateFlow()

    suspend fun notifyAuthExpired() {
        tokenManager.clearTokens()
        _loginRequired.value = true
    }

    fun onLoginCompleted() {
        _loginRequired.value = false
    }
}