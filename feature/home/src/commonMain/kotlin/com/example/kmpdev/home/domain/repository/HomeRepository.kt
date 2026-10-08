package com.example.kmpdev.home.domain.repository

import com.example.kmpdev.home.domain.model.Page
import com.example.kmpdev.home.domain.model.TestModel
import com.example.rt.domain.model.ResponseBody
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import kotlinx.serialization.json.JsonObject

interface HomeRepository {

    @POST("/app/page/{Id}")
    suspend fun getPostList(
        @Path("Id") id: Int,
        @Body json: JsonObject
    ): ResponseBody<Page<TestModel>>
}