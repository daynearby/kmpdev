package com.example.rt.domain.repository

import com.example.rt.domain.model.LoginRequest
import com.example.rt.domain.model.LoginResponse
import com.example.rt.domain.model.ResponseBody

/**
 * 登录接口（普通接口，定义于 shared）
 *
 * 实现在 :feature:user（AuthApiImpl 内部委托给 Ktorfit 接口）。
 * 这样 shared 的 [com.example.rt.domain.usecase.LoginUseCase] 可依赖本接口，
 * 而无需 shared 引入 Ktorfit 注解 / 插件。
 */
interface AuthApi {
    suspend fun login(request: LoginRequest): ResponseBody<LoginResponse>
}
