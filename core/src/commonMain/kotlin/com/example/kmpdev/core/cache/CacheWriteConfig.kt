package com.example.kmpdev.core.cache

import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 *   - `companion object Key` 是标准协程上下文 Key 模式（参考 `CoroutineName`），
 *     Key 本身是类型标识，不需要"赋值"。元素通过 `withContext(this)` 注入，
 *     插件通过 `coroutineContext[CacheWriteConfig.Key]` 读取。
 *   - `withContext` 每次创建独立的作用域，多个并发的 `wrap()` 互不干扰。
 *   - `AbstractCoroutineContextElement(CacheWriteConfig)` 中的 `CacheWriteConfig`
 *     是 companion object 引用（即 `CacheWriteConfig.Key`），这是 Kotlin 标准写法。
 */
class CacheWriteConfig(
    val cacheKey: String,
    val ttlSeconds: Long
) : AbstractCoroutineContextElement(CacheWriteConfig) {
    companion object Key : CoroutineContext.Key<CacheWriteConfig>

    suspend fun <T> wrap(block: suspend () -> T): T =
        withContext(this) { block() }
}