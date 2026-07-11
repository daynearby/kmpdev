package com.example.kmpdev.core.di

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.getSystemService
import com.example.kmpdev.core.app.AppVersion
import com.example.kmpdev.core.cache.SecureStorage
import com.tencent.mmkv.MMKV
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * 使用context入口，主要是全局context
 */

val coreModuleAndroid = module {
    single<AppVersion> { AppVersion(androidContext()) }
    single { SecureStorage(androidContext()) }
}

class PlatformConfig(private val context: Context) {

    val pm: PackageManager? by lazy {
        context.getSystemService<PackageManager>()
    }

    fun versionCode(): Long {
        MMKV.defaultMMKV()
        return pm?.getPackageInfo(context.packageName, 0)?.let { packageInfo ->
            if (Build.VERSION_CODES.P > Build.VERSION.SDK_INT) {
                packageInfo.versionCode.toLong()
            } else {
                packageInfo.longVersionCode
            }
        } ?: 1L
    }

}