package com.example.kmpdev.core.network.auth

/**
 * 刷新token的接口，通过koin注入实现类的方式，
 * 最终在tokenRefreshManager中执行，调用refresh进行刷新token
 * TODO:在user层实现该接口，并且通过koin注入
 */
interface TokenRefresher {
    suspend fun refresh(refreshToken: String): AuthToken
}