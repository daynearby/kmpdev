package com.example.kmpdev.core.di

import com.example.kmpdev.core.app.AppVersion
import org.koin.dsl.module

val coreModuleIOS = module {
    single { AppVersion() }
}