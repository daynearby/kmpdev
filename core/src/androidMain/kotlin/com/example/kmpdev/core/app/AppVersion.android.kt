package com.example.kmpdev.core.app

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

actual class AppVersion(context: Context) {
    private var packageInfo: PackageInfo =
        context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)

    actual val versionName: String
        get() =
            packageInfo.versionName ?: "com.example.kmpdev"


    actual val versionCode: Long
        get() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }


}