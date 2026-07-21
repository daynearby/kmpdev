package com.example.user.di

import com.example.kmpdev.core.network.auth.TokenRefresher
import com.example.user.domain.repository.createTokenRefreshRepository
import com.example.user.domain.usecase.TokenRefreshUseCase
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.dsl.module

val userModule = module {
    single { get<Ktorfit>().createTokenRefreshRepository() }
    single<TokenRefresher> { TokenRefreshUseCase( get()) }
}