package com.example.user.domain.api

import com.example.rt.domain.model.ResponseBody
import com.example.rt.domain.model.User
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path

/**
 * 用户接口（Ktorfit，实现由 KSP 生成 createUserApi()）
 */
interface UserApi {

    @GET("user/{id}")
    suspend fun getUser(@Path("id") id: String): ResponseBody<User>
}
