package com.tlpcraft.adbdesktop.core.logger

import co.touchlab.kermit.Logger

class KermitAppLogger(private val logger: Logger = Logger) : AppLogger {
    override fun debug(tag: String, message: String) = logger.d(tag = tag) { message }

    override fun info(tag: String, message: String) = logger.i(tag = tag) { message }

    override fun warn(tag: String, message: String, throwable: Throwable?) = logger.w(tag = tag, throwable = throwable) { message }

    override fun error(tag: String, message: String, throwable: Throwable?) = logger.e(tag = tag, throwable = throwable) { message }
}
