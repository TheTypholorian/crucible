@file:JvmName("Log")
package net.typho.crucible

import net.typho.misc_util.KtServiceLoader
import org.slf4j.ILoggerFactory
import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.MarkerFactory
import org.slf4j.event.Level
import org.slf4j.helpers.AbstractLogger
import org.slf4j.helpers.BasicMDCAdapter
import org.slf4j.helpers.MessageFormatter
import org.slf4j.spi.MDCAdapter
import org.slf4j.spi.SLF4JServiceProvider
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.math.floor

@JvmField
@get:JvmName("INSTANCE")
val LOG = KtServiceLoader.load(ILog::class.java).firstOrNull()?.get() ?: LogImpl

fun Throwable.fullToString(): String {
    val out = ByteArrayOutputStream()
    val writer = PrintStream(out, false)
    printStackTrace(writer)
    out.flush()
    return String(out.toByteArray())
}

interface ILog {
    interface StatusUpdater {
        var progress: Float

        fun finish()
    }

    fun info(msg: String)

    fun info(msg: Any?) = info(msg.toString())

    fun info(msg: Any?, t: Throwable) = info(msg.toString() + "\n" + t.fullToString())

    fun warn(msg: String)

    fun warn(msg: Any?) = warn(msg.toString())

    fun warn(msg: Any?, t: Throwable) = warn(msg.toString() + "\n" + t.fullToString())

    fun debug(msg: String)

    fun debug(msg: Any?) = debug(msg.toString())

    fun debug(msg: Any?, t: Throwable) = debug(msg.toString() + "\n" + t.fullToString())

    fun error(msg: String)

    fun error(msg: Any?) = error(msg.toString())

    fun error(msg: Any?, t: Throwable) = error(msg.toString() + "\n" + t.fullToString())

    fun status(unit: String, max: Float, scale: Float = 1f, width: Int = 20): StatusUpdater

    fun statusPercent(scale: Float = 1f, width: Int = 20): StatusUpdater = status("%", 100f, scale, width)
}

private object LogImpl : ILog {
    override fun info(msg: String) {
        println(msg)
    }

    override fun warn(msg: String) {
        info("\u001b[93m$msg\u001b[0m")
    }

    override fun debug(msg: String) {
        if (Config.debug) {
            info(msg)
        }
    }

    override fun error(msg: String) {
        System.err.println(msg)
    }

    override fun status(
        unit: String,
        max: Float,
        scale: Float,
        width: Int
    ): ILog.StatusUpdater {
        return object : ILog.StatusUpdater {
            var ended = false

            override var progress: Float = 0f
                set(value) {
                    if (ended) {
                        throw IllegalStateException("Status updater already ended")
                    }

                    ended = value >= max
                    field = value.coerceAtMost(max).coerceAtLeast(0f)
                    val amount = floor(field / max * width).toInt()
                    print("\r[" + "#".repeat(amount) + "_".repeat(width - amount) + "] - " + "%.1f/%.1f $unit".format(field * scale, max * scale))

                    if (ended) {
                        println()
                    }

                    System.out.flush()
                }

            init {
                progress = 0f
            }

            override fun finish() {
                progress = max
            }
        }
    }
}

class SLF4JServiceProviderImpl : SLF4JServiceProvider, ILoggerFactory {
    private val mdc = BasicMDCAdapter()

    override fun getLoggerFactory() = this

    override fun getMarkerFactory() = MarkerFactory.getIMarkerFactory()

    override fun getMDCAdapter(): MDCAdapter = mdc

    override fun getRequestedApiVersion() = "2.0.99"

    override fun initialize() = Unit

    override fun getLogger(name: String): Logger = object : AbstractLogger() {
        override fun getFullyQualifiedCallerName() = null

        override fun handleNormalizedLoggingCall(
            level: Level,
            marker: Marker?,
            messagePattern: String,
            arguments: Array<out Any?>?,
            throwable: Throwable?
        ) {
            var msg = if (arguments == null) {
                messagePattern
            } else {
                val format = if (throwable == null) {
                    MessageFormatter.arrayFormat(messagePattern, arguments)
                } else {
                    MessageFormatter.arrayFormat(messagePattern, arguments, throwable)
                }

                if (format.throwable == null) format.message else format.message + "\n" + format.throwable.fullToString()
            }

            if (marker != null) {
                msg = marker.toString() + msg
            }

            when (level) {
                Level.ERROR -> LOG.error(msg)
                Level.WARN -> LOG.warn(msg)
                Level.DEBUG -> LOG.debug(msg)
                else -> LOG.info(msg)
            }
        }

        override fun isTraceEnabled() = Config.debug

        override fun isTraceEnabled(marker: Marker) = Config.debug

        override fun isDebugEnabled() = Config.debug

        override fun isDebugEnabled(marker: Marker) = Config.debug

        override fun isInfoEnabled() = true

        override fun isInfoEnabled(marker: Marker) = true

        override fun isWarnEnabled() = true

        override fun isWarnEnabled(marker: Marker) = true

        override fun isErrorEnabled() = true

        override fun isErrorEnabled(marker: Marker) = true
    }
}