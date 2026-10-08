package com.example.rt.extension

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build


inline val Context.versionName: String
    get() {
        val packageInfo =
            packageManager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES)
        return packageInfo.versionName ?: packageInfo.versionCode.toString()
    }

inline val Context.versionCode: Long
    get() {
        return packageManager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES).let { packageInfo ->
            if (Build.VERSION_CODES.P > Build.VERSION.SDK_INT) {
                packageInfo.versionCode.toLong()
            } else {
                packageInfo.longVersionCode
            }
        }

    }