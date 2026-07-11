package com.example.kmpdev.core.network

sealed class RequestResult<out T> {
    /** 缓存命中（仅 CACHE_THEN_NETWORK） */
    data class FromCache<T>(val data: T) : RequestResult<T>()

    /** 网络请求成功 */
    data class FromNetwork<T>(val data: T) : RequestResult<T>()

    /** 网络失败但有缓存兜底 → Toast 提示，不刷新 UI */
    data class NetworkErrorWithCache<T>(
        val error: NetworkError,
        val toastMessage: String
    ) : RequestResult<T>()

    /** 请求失败且无缓存 → ErrorBanner + 重试 */
    data class Error(
        val error: NetworkError
    ) : RequestResult<Nothing>()
}