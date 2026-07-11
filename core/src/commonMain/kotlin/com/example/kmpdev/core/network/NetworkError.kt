package com.example.kmpdev.core.network

/**
 * 网络错误分类。
 * ViewModel 根据子类型决定展示什么信息给用户。
 */
sealed class NetworkError {
    /** 本地网络不可用 / 连接超时 / DNS 解析失败 */
    data class NoConnectivity(val cause: Throwable? = null) : NetworkError()

    /** 服务端返回了 HTTP 错误状态码 */
    data class HttpError(
        val statusCode: Int,
        val serverMessage: String? = null
    ) : NetworkError()

    /** JSON 解析失败 / 数据格式异常 */
    data class Serialization(val cause: Throwable) : NetworkError()

    /** 未归类的其他错误 */
    data class Unknown(val cause: Throwable) : NetworkError()
}