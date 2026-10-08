package com.example.user.di

import com.example.kmpdev.core.cache.CacheManager
import com.example.kmpdev.core.network.auth.TokenRefresher
import com.example.rt.domain.repository.AuthApi
import com.example.rt.domain.repository.UserRepository
import com.example.user.data.AuthApiImpl
import com.example.user.data.UserRepositoryImpl
import com.example.user.domain.api.AuthApiKtorfit
import com.example.user.domain.api.UserApi
import com.example.user.domain.api.createAuthApiKtorfit
import com.example.user.domain.api.createUserApi
import com.example.user.domain.repository.TokenRefreshRepository
import com.example.user.domain.repository.createTokenRefreshRepository
import com.example.user.domain.usecase.TokenRefreshUseCase
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.dsl.module

val userModule = module {
    // ── token 刷新 ──
    single { get<Ktorfit>().createTokenRefreshRepository() }
    single<TokenRefresher> { TokenRefreshUseCase(get()) }

    // ── AuthApi：Ktorfit 接口 → 实现类（供 shared 的 LoginUseCase 使用）──
    single { get<Ktorfit>().createAuthApiKtorfit() }
    single<AuthApi> { AuthApiImpl(get()) }

    // ── UserApi ──
    single { get<Ktorfit>().createUserApi() }

    // ── ★ 绑定 shared 接口 → feature 实现 ──
    single<UserRepository> { UserRepositoryImpl(userApi = get(), cacheManager = get()) }
}