package com.example.user.data

import com.example.kmpdev.core.cache.CacheManager
import com.example.rt.domain.model.User
import com.example.rt.domain.repository.UserRepository
import com.example.user.domain.api.UserApi
import kotlinx.serialization.json.Json

/**
 * [UserRepository] 的 feature 实现。
 *
 * - 远端数据经 [UserApi]（Ktorfit）获取，并解包统一响应体、判断 code；
 * - 本地持久化当前用 [CacheManager]（无 DB 表时兜底），后续可替换为 SQLDelight。
 */
class UserRepositoryImpl(
    private val userApi: UserApi,
    private val cacheManager: CacheManager
) : UserRepository {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun fetchUser(id: String): User {
        val body = userApi.getUser(id)
        if (body.code != 2000) {
            throw RuntimeException("fetchUser failed: code=${body.code} msg=${body.msg}")
        }
        return body.data
    }

    override suspend fun saveUserLocally(user: User) {
        cacheManager.put("local:user:${user.id}", json.encodeToString(user))
    }
}
