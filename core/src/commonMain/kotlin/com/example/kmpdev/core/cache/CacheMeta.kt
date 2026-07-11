package com.example.kmpdev.core.cache

/**
 * 保存cache的meta信息
 */
data class CacheMeta(
    val cachedAt: Long,
    val ttlSeconds: Long,
    val versionCode: Long,
)

