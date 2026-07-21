package com.example.user.domain.repository

import com.example.kmpdev.core.network.auth.AuthToken
import com.example.rt.domain.model.ResponseBody
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST

/**
 * token刷新接口
 */
interface TokenRefreshRepository {

    @POST("token/refresh")
    suspend fun refreshToken(@Body refreshToken : String) : ResponseBody<AuthToken>
}