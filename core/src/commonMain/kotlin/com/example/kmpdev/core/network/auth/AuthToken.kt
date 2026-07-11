package com.example.kmpdev.core.network.auth

data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenValidTime: Long = 0 // 暂定
)
