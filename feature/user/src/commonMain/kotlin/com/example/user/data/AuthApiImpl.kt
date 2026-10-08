package com.example.user.data

import com.example.rt.domain.model.LoginRequest
import com.example.rt.domain.model.LoginResponse
import com.example.rt.domain.model.ResponseBody
import com.example.rt.domain.repository.AuthApi
import com.example.user.domain.api.AuthApiKtorfit

/**
 * shared [AuthApi] 的 feature 实现，委托给 Ktorfit 接口。
 */
class AuthApiImpl(
    private val api: AuthApiKtorfit
) : AuthApi {

    override suspend fun login(request: LoginRequest): ResponseBody<LoginResponse> =
        api.login(request)
}
