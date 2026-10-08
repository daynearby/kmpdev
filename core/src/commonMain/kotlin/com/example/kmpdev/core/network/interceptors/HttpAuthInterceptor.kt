package com.example.kmpdev.core.network.interceptors

import com.example.kmpdev.core.app.RequestParams
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.headers

/**
 * 调用挂起方法需要包装一层
 */
class HttpAuthInterceptorConfig {
    var tokenProvider: suspend () -> String? = { null }
    var headerProvider : suspend ()-> RequestParams = { RequestParams.empty()}
}

val HttpAuthInterceptor = createClientPlugin("HttpAuthInterceptor", ::HttpAuthInterceptorConfig) {
    val config = pluginConfig     // 在 builder scope 捕获

    onRequest { request, _ ->
        val token = config.tokenProvider()
        val header = config.headerProvider()
        if (token != null) {
            // 可能在header里面增加更多参数
            // TODO: 还有一些检验签名参数，可以通过APPVersion的类进行注入，在理进行加载
            //  request.headers.append(HttpHeaders.Authorization, "Bearer $token")
            request.headers.append("token", token)
        }
        request.headers {
            set("timestamp",header.time)
            set("device",header.device)
            set("version",header.name)
            set("versionCode",header.code)
            set("guestId",header.id)
            set("sign",header.string)
            set("clientType",header.type)
        }
    }
}