package com.example.user.domain.api

import com.example.rt.domain.model.LoginRequest
import com.example.rt.domain.model.LoginResponse
import com.example.rt.domain.model.ResponseBody
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST

/**
 * 登录接口（Ktorfit，实现由 KSP 生成 createAuthApiKtorfit()）
 *
 * 仅用于 :feature:user 内部；对外暴露的是 shared 的普通接口 [com.example.rt.domain.repository.AuthApi]。
 */
interface AuthApiKtorfit {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ResponseBody<LoginResponse>
}
