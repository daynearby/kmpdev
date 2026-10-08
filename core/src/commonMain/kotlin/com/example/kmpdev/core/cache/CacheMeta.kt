package com.example.kmpdev.core.cache

import kotlinx.serialization.Serializable

/**
 * 保存cache的meta信息
 */
@Serializable
data class CacheMeta(
    val cachedAt: Long,
    val ttlSeconds: Long,
    val versionCode: Long,
)

