package com.example.kmpdev.core.network.error

import com.example.kmpdev.core.network.NetworkError

/**
 * 解析用户可读错误消息。
 * 优先级：服务端返回 msg → HTTP 状态码固定文案 → NetworkError 子类型默认文案
 */
object ErrorMessageResolver {

    fun resolve(
        error: NetworkError,
        serverMessage: String? = null   // 从 ResponseBody.msg 或 HttpError.message 传入
    ): String {
        // 第一优先级：服务端返回的非空消息
        if (!serverMessage.isNullOrBlank()) return serverMessage

        // 第二优先级：HTTP 状态码固定文案
        if (error is NetworkError.HttpError) {
            return when (error.statusCode) {
                401 -> "登录已过期，请重新登录"
                403 -> "没有访问权限"
                404 -> "请求的资源不存在"
                429 -> "请求过于频繁，请稍后再试"
                in 500..599 -> "服务器繁忙，请稍后再试"
                else -> "请求失败 (${error.statusCode})"
            }
        }

        // 第三优先级：NetworkError 子类型默认文案
        return when (error) {
            is NetworkError.NoConnectivity -> "网络连接不可用，请检查网络设置"
            is NetworkError.Serialization -> "数据解析失败"
            is NetworkError.HttpError -> "请求失败"       // 兜底（无 statusCode 场景）
            is NetworkError.Unknown -> "未知错误"
        }
    }
}