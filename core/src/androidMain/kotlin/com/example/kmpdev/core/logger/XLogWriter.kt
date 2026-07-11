package com.example.kmpdev.core.logger

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.piasy.kmp.xlog.Logging

class XLogWriter : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        val msg = if (throwable != null) {
            "$message\n${throwable.stackTraceToString()}"
        } else {
            message
        }
        when (severity) {
            Severity.Verbose -> Logging.debug(tag, msg)
            Severity.Debug   -> Logging.debug(tag, msg)
            Severity.Info    -> Logging.info(tag, msg)
            Severity.Warn    -> Logging.error(tag, msg)   // ★ Logging 无 Warn 级别
            Severity.Error   -> Logging.error(tag, msg)
            Severity.Assert  -> Logging.error(tag, msg)   // ★ Logging 无 Assert 级别
        }
    }
}