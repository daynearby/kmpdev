package com.example.rt

import android.app.Application
import android.content.pm.ApplicationInfo
import com.example.kmpdev.core.app.RequestParams
import com.example.kmpdev.core.cache.initMMKV
import com.example.kmpdev.core.di.coreModule
import com.example.kmpdev.core.di.coreModuleAndroid
import com.example.kmpdev.core.logger.initLogStorage
import com.example.kmpdev.core.util.AppConfig
import com.example.kmpdev.home.di.homeModule
import com.example.user.di.userModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File

class App : Application() {

    private val appModule = module {

        single<String>(named(AppConfig.BASE_URL)) {   /*这里应该直接区分开发环境进行注入*/ "https://baidu.com/" }
        single { initRequestParams() }
    }

    override fun onCreate() {
        super.onCreate()
        // ① XLog 必须先初始化（子目录隔离）
        initCacheConfig()
        initMMKV(this)
        startKoin {
            androidContext(this@App)
            modules(appModule, coreModule, coreModuleAndroid, userModule, homeModule)
        }

    }


    private fun initCacheConfig() {
        val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        var logCacheDir = File(externalCacheDir, "logCache")

        if (!logCacheDir.mkdirs()) {
            logCacheDir = File(cacheDir, "logCache")
            logCacheDir.mkdirs()
        }

        initLogStorage(context = this, logDir = logCacheDir.absolutePath, isDebug = isDebug)


    }

    private fun initRequestParams(): RequestParams {

        return RequestParams("", "", "", "", "", "", "")
    }

}