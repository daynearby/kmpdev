package com.example.kmpdev.core.cache

import com.ctrip.flight.mmkv.initialize

actual fun initMMKV(param: Any) {
    val rootDir = param as String    // e.g. NSSearchPathForDirectoriesInDomains 结果
    initialize(rootDir)
}