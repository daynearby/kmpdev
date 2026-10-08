package com.example.rt.domain.model

import kotlinx.serialization.Serializable

/**
 * 响应数据结构
 */
@Serializable
data class ResponseBody<T>(
    val data: T,
    val state: String = "",
    val code: Int = 2000,
    val msg: String = ""
)