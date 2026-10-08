package com.example.rt.domain.usecase

import com.example.rt.domain.model.User
import com.example.rt.domain.repository.UserRepository

/**
 * 保存用户到本地（不走网络，不进缓存策略）
 */
class SaveUserUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(user: User) = repository.saveUserLocally(user)
}
