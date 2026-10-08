package com.example.rt.di

import com.example.kmpdev.core.network.CacheThenNetworkStrategy
import com.example.rt.domain.repository.AuthApi
import com.example.rt.domain.repository.UserRepository
import com.example.rt.domain.usecase.GetUserUseCase
import com.example.rt.domain.usecase.LoginUseCase
import com.example.rt.domain.usecase.SaveUserUseCase
import org.koin.dsl.module

/**
 * 注册 shared 层通用 UseCase（factory：每次注入创建新实例）。
 *
 * 依赖来自其它模块、运行时由 Koin 解析：
 * - [UserRepository] / [AuthApi] 接口绑定在 :feature:user
 * - [CacheThenNetworkStrategy] 单例在 core
 */
val shareModule = module {
    factory { GetUserUseCase(repository = get(), strategy = get()) }
    factory { LoginUseCase(strategy = get(), authApi = get()) }
    factory { SaveUserUseCase(repository = get()) }
}