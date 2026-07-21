package com.example.kmpdev.core.network

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient

/**
 * 创建Ktorfit 对象
 */
fun createKtorfit(
    httpClient: HttpClient,
    baseUrl: String,
): Ktorfit = Ktorfit.Builder()
    .httpClient(httpClient)
    .baseUrl(baseUrl)  // 按环境切换
    .build()