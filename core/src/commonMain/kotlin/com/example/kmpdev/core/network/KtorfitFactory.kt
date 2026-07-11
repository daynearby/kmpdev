package com.example.kmpdev.core.network

import com.example.kmpdev.core.cache.CacheManager
import de.jensklingenberg.ktorfit.Ktorfit

/**
 * 创建Ktorfit 对象
 */
fun createKtorfit(cacheManager: CacheManager,baseUrl : String): Ktorfit = Ktorfit.Builder()
    .httpClient(createHttpClient(cacheManager))
    .baseUrl(baseUrl)  // 按环境切换
    .build()