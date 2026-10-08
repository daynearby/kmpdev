package com.example.rt.domain.model

import kotlinx.serialization.Serializable

/**
 * 登录请求
 */
@Serializable
data class LoginRequest(
    val username: String = "",
    val password: String = ""
)

/**
 * 登录响应（API 协议层字段，扁平化、可序列化）
 *
 * 注：不包含 [com.example.kmpdev.core.network.auth.AuthToken]，
 * 因 AuthToken 非 @Serializable，避免影响 kotlinx.serialization 代码生成。
 */
@Serializable
data class LoginResponse(
    val accessToken: String = "",
    val refreshToken: String = "",
    val accessTokenValidTime: Long = 0L
)
