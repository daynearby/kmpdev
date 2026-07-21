package com.example.kmpdev.home.di

import com.example.kmpdev.home.domain.repository.createHomeRepository
import com.example.kmpdev.home.domain.usecase.GetFeedUseCase
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.dsl.module


val homeModule = module {
    single {
        GetFeedUseCase(get(), get<Ktorfit>().createHomeRepository())
    }
}