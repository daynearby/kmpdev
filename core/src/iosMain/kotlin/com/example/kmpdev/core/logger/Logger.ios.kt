package com.example.kmpdev.core.logger

import co.touchlab.kermit.LoggerConfig
import co.touchlab.kermit.NSLogWriter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import com.piasy.kmp.xlog.Logging
import com.piasy.kmp.xlog.initializeMarsXLog
import kotlin.experimental.ExperimentalNativeApi

actual fun createPlatformLoggerConfig(): LoggerConfig = loggerConfigInit(
    NSLogWriter(),     // ★ 输出到 iOS NSLog / Console.app
    XLogWriter(),        // ★ 同时写入 xlog 文件
    minSeverity = Severity.Verbose   // ★ 放行所有级别，实际过滤由 Logger wrapper 控制
)

actual fun initLogStorage(context: Any?, logDir: String, isDebug: Boolean) {
    // ★ iOS 版 initializeMarsXLog 不需要 context / logDir（库内部自己选路径）
    initializeMarsXLog(
        level = if (isDebug) Logging.LEVEL_DEBUG else Logging.LEVEL_INFO,  // ★ Mars 规则
        namePrefix = "kmpdev"
    )
}

@OptIn(ExperimentalNativeApi::class)
actual fun isDebugBuild(): Boolean = Platform.isDebugBinary