package com.example.kmpdev.core.network

/**
 * 配合android平台类型，
 * iOS的类型已经通过：Ktor Darwin 引擎将底层 NSError 包装成 Ktor 自己的异常类型
 *
 * 正常来说不会跑到这里
 */
actual fun isNetworkIOException(e: Exception): Boolean = false