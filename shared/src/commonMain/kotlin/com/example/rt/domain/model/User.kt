package com.example.rt.domain.model

import kotlinx.serialization.Serializable

/**
 * 业务层用户模型（与 API 响应体解耦，ViewModel/UI 只感知此类型）
 */
@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val avatar: String = ""
)
