package com.example.kmpdev.core.cache

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * 可能是需要进行缓存清理，只清理过期的缓存（设置了时长的缓存）
 *
 * 并且 CacheManager.trimCache 方法清理一半的缓存
 */
class CacheEvictionScheduler(
    private val cacheManager: CacheManager,
    private val scope: CoroutineScope
) {
    fun start(intervalMinutes: Long = 30) {
        scope.launch {
            while (true) {
                delay((intervalMinutes * 60 * 1000).milliseconds)
                cacheManager.evictExpired()
            }
        }
    }
}