package com.example.rt.domain.usecase

import com.example.kmpdev.core.cache.CachePolicy
import com.example.kmpdev.core.network.CacheThenNetworkStrategy
import com.example.kmpdev.core.network.RequestResult
import com.example.rt.domain.model.LoginRequest
import com.example.rt.domain.model.LoginResponse
import com.example.rt.domain.model.ResponseBody
import com.example.rt.domain.repository.AuthApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 登录（不走缓存，但 Strategy 内仍包裹 401→刷新→重试）
 *
 * AuthApi 返回统一响应体 [ResponseBody]，这里在 UseCase 内解包为业务类型 [LoginResponse]，
 * 保持 Flow 的 [RequestResult] 结构不变（code != 2000 时转为 Error）。
 */
class LoginUseCase(
    private val strategy: CacheThenNetworkStrategy,
    private val authApi: AuthApi
) {
    suspend operator fun invoke(request: LoginRequest): Flow<RequestResult<LoginResponse>> =
        strategy.execute<ResponseBody<LoginResponse>>(
            policy = CachePolicy.NO_CACHE,
            cacheKey = "",
            fetcher = { authApi.login(request) }
        ).map { result ->
            when (result) {
                is RequestResult.FromCache ->
                    RequestResult.FromCache(result.data.data)

                is RequestResult.FromNetwork ->
                    RequestResult.FromNetwork(result.data.data)

                is RequestResult.NetworkErrorWithCache ->
                    RequestResult.NetworkErrorWithCache(result.error, result.toastMessage)

                is RequestResult.Error -> result
            }
        }
}
