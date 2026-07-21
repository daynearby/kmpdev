package com.example.kmpdev.core.app

/**
 *
 * 请求头验证参数
 *
 */
data class RequestParams(
    val time: String,
    val device: String,
    val name: String,
    val code: String,
    val id: String,
    val string: String,
    val type: String,
) {
    companion object {

        /**
         * 初始化参数
         */
        fun empty(): RequestParams = RequestParams("", "", "", "", "", "", "")
    }
}
