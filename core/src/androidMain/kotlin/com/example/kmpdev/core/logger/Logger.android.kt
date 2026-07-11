package com.example.kmpdev.core.logger

import android.content.Context
import co.touchlab.kermit.LogcatWriter
import co.touchlab.kermit.LoggerConfig
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import com.piasy.kmp.xlog.Logging
import com.piasy.kmp.xlog.initializeMarsXLog

@Volatile
private var _isDebug: Boolean? = null

actual fun createPlatformLoggerConfig(): LoggerConfig = loggerConfigInit(
    LogcatWriter(),   // ★ 输出到 Android Logcat
    XLogWriter(),       // ★ 同时写入 xlog 文件
    minSeverity = Severity.Verbose   // ★ 放行所有级别，实际过滤由 Logger wrapper 控制
)

actual fun initLogStorage(context: Any?, logDir: String, isDebug: Boolean) {
    val ctx = context as Context   // ★ Android 需要 Application Context
    _isDebug = isDebug
    initializeMarsXLog(
        context = ctx,                                                   // ★ Mars 规则要求 Context
        logDir = logDir,                                                 // ★ 独立子目录
        level = if (isDebug) Logging.LEVEL_DEBUG else Logging.LEVEL_INFO,// ★ Mars 规则
        namePrefix = "kmpdev"
    )

}

actual fun isDebugBuild(): Boolean = _isDebug ?: error("isDebugBuild() 在 initLogStorage() 之前调用，请确保先初始化日志模块")