package com.example.kmpdev.core.cache

import android.content.Context
import com.ctrip.flight.mmkv.initialize

actual fun initMMKV(param: Any) {
    val context = param as Context
    val rootDir = initialize(context)
}