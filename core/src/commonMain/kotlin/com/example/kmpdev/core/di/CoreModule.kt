package com.example.kmpdev.core.di

import com.ctrip.flight.mmkv.defaultMMKV
import com.example.kmpdev.core.app.AppVersion
import com.example.kmpdev.core.cache.CacheManager
import com.example.kmpdev.core.cache.MMKVCacheManagerImpl
import com.example.kmpdev.core.network.CacheThenNetworkStrategy
import com.example.kmpdev.core.network.auth.AuthCoordinator
import com.example.kmpdev.core.network.auth.AuthRetryHandler
import com.example.kmpdev.core.network.auth.TokenManager
import com.example.kmpdev.core.network.auth.TokenManagerImpl
import com.example.kmpdev.core.network.auth.TokenRefreshManager
import com.example.kmpdev.core.network.createHttpClient
import com.example.kmpdev.core.network.createKtorfit
import com.example.kmpdev.core.util.AppConfig
import org.koin.core.qualifier.named
import org.koin.dsl.module

val coreModule = module {
    //缓存
    single { defaultMMKV() }
    single<CacheManager> { MMKVCacheManagerImpl(get(), get<AppVersion>().versionCode) }
    //single { CacheEvictionScheduler(get(), get()) }

    // ── HttpClient（安装了 HttpCachePlugin、HttpRequestRetry、HttpAuthInterceptor）──
    single { createHttpClient(cacheManager = get(), tokenManager = get(), get()) }

    // ── Ktorfit（类型安全 API）──
    single {
        createKtorfit(
            get(),
            get<String>(named(AppConfig.BASE_URL)),
        )
    }

    // ── 缓存策略（单例，所有 Repository 共享）──
    single { CacheThenNetworkStrategy(get(), get()) }

    // ── Auth ──
    single<TokenManager> { TokenManagerImpl(get()) }
    single { AuthCoordinator(get()) }
    single { TokenRefreshManager(get(), get()) }   // token刷新
    single { AuthRetryHandler(get(), get()) }      // 401→刷新→重试 完整链路

}