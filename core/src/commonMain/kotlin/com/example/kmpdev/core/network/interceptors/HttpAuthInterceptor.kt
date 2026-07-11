package com.example.kmpdev.core.network.interceptors

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders

/**
 * 调用挂起方法需要包装一层
 */
class HttpAuthInterceptorConfig {
    var tokenProvider: suspend () -> String? = { null }
}

val HttpAuthInterceptor = createClientPlugin("HttpAuthInterceptor", ::HttpAuthInterceptorConfig) {
    val config = pluginConfig     // 在 builder scope 捕获

    onRequest { request, _ ->
        val token = config.tokenProvider()
        if (token != null) {
            // 可能在header里面增加更多参数
            // TODO: 还有一些检验签名参数，可以通过APPVersion的类进行注入，在理进行加载
            request.headers.append(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}