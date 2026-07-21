package com.example.kmpdev.home.domain.model

import kotlinx.serialization.Serializable

/**
 * 单页数据
 */
@Serializable
data class Page<T>(
    var current: Int = 0,
    var data: List<T> = emptyList()
)
