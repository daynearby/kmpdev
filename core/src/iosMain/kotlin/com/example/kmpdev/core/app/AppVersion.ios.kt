package com.example.kmpdev.core.app

import platform.Foundation.NSBundle

actual class AppVersion {
    actual val versionName: String =
        NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "com.example.kmpdev"
    actual val versionCode: Long =
        (NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String)?.toLongOrNull() ?: 0L
}