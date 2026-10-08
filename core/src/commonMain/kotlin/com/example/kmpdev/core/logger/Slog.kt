package com.example.kmpdev.core.logger

import co.touchlab.kermit.LoggerConfig
import co.touchlab.kermit.Severity
import kotlin.concurrent.Volatile
import co.touchlab.kermit.Logger as KermitLogger

object Slog {
    private val logger: KermitLogger = KermitLogger(createPlatformLoggerConfig())

    /**
     * ★ 自己维护 severity，不依赖 Kermit 的 minSeverity。
     *   Severity 是 enum（Verbose < Debug < Info < Warn < Error < Assert），
     *   用 `minSeverity <= targetLevel` 判断是否需要输出。 */
    @Volatile
    private var minSeverity: Severity = Severity.Debug

    fun setMinSeverity(severity: Severity) {
        minSeverity = severity
    }

    fun v(tag: String, message: String) {
        if (minSeverity <= Severity.Verbose) logger.v(tag = tag) { message }
    }
    fun d(tag: String, message: String) {
        if (minSeverity <= Severity.Debug) logger.d(tag = tag) { message }
    }
    fun i(tag: String, message: String) {
        if (minSeverity <= Severity.Info) logger.i(tag = tag) { message }
    }
    fun w(tag: String, message: String) {
        if (minSeverity <= Severity.Warn) logger.w(tag = tag) { message }
    }
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (minSeverity <= Severity.Error) logger.e(throwable, tag = tag) { message }
    }

}

expect fun createPlatformLoggerConfig(): LoggerConfig

expect fun initLogStorage(context: Any?, logDir: String, isDebug: Boolean)

expect fun isDebugBuild(): Boolean