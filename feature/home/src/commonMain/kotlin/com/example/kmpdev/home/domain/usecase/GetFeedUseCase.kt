package com.example.kmpdev.home.domain.usecase

import com.example.kmpdev.core.cache.CachePolicy
import com.example.kmpdev.core.network.CacheThenNetworkStrategy
import com.example.kmpdev.core.network.RequestResult
import com.example.kmpdev.home.domain.model.Page
import com.example.kmpdev.home.domain.model.TestModel
import com.example.kmpdev.home.domain.repository.HomeRepository
import com.example.rt.domain.model.ResponseBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GetFeedUseCase(val strategy: CacheThenNetworkStrategy, val homeRepository: HomeRepository) {

    suspend operator fun invoke(id: Int = 1, current: Int = 1): Flow<Page<TestModel>> {


        val jsonObject = buildJsonObject {
            put("id", 1)
            put("size", 20)
        }
        val requestResult: Flow<RequestResult<ResponseBody<Page<TestModel>>>> =
            strategy.execute(
                if (current == 1) CachePolicy.CACHE_THEN_NETWORK else CachePolicy.NO_CACHE,
                "feed:page:id$id"
            ) {
                homeRepository.getPostList(id, jsonObject)
            }

        return requestResult.map { result ->
            when (result) {
                is RequestResult.FromCache<ResponseBody<Page<TestModel>>> -> result.data.data
                is RequestResult.FromNetwork<ResponseBody<Page<TestModel>>> -> result.data.data
                is RequestResult.NetworkErrorWithCache -> Page<TestModel>()
                is RequestResult.Error -> {
                    Page<TestModel>()
                }
            }
        }
    }

}