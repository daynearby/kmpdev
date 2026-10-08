package com.example.rt.domain.usecase

import com.example.kmpdev.core.cache.CachePolicy
import com.example.kmpdev.core.network.CacheThenNetworkStrategy
import com.example.kmpdev.core.network.RequestResult
import com.example.rt.domain.model.User
import com.example.rt.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

/**
 * 获取用户（缓存优先：先展示缓存，再静默刷新网络）
 *
 * 缓存由 [CacheThenNetworkStrategy] 透明处理，UseCase 不感知 CacheManager。
 * Repository 负责 ResponseBody 解包，这里拿到的始终是业务类型 [User]。
 */
class GetUserUseCase(
    private val repository: UserRepository,
    private val strategy: CacheThenNetworkStrategy
) {
    suspend operator fun invoke(userId: String): Flow<RequestResult<User>> =
        strategy.execute<User>(
            policy = CachePolicy.CACHE_THEN_NETWORK,
            cacheKey = "GET|users/$userId",
            ttlSeconds = 300L,
            fetcher = { repository.fetchUser(userId) }
        )
}
