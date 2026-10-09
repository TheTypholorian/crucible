@file:JvmName("Log")
package net.typho.crucible.wrapper.log

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
var debugEnabled = false

fun Throwable.fullToString(): String {
    val out = ByteArrayOutputStream()
    val writer = PrintStream(out, false)
    printStackTrace(writer)
    out.flush()
    return String(out.toByteArray())
}

interface StatusUpdater {
    var progress: Float

    fun finish()
}

fun info(msg: String) {
    println(msg)
}

fun info(msg: Any?) = info(msg.toString())

fun info(msg: Any?, t: Throwable) = info(msg.toString() + "\n" + t.fullToString())

fun warn(msg: String) {
    info("\u001b[93m$msg\u001b[0m")
}

fun warn(msg: Any?) = warn(msg.toString())

fun warn(msg: Any?, t: Throwable) = warn(msg.toString() + "\n" + t.fullToString())

fun debug(msg: String) {
    if (debugEnabled) {
        info(msg)
    }
}

fun debug(msg: Any?) = debug(msg.toString())

fun debug(msg: Any?, t: Throwable) = debug(msg.toString() + "\n" + t.fullToString())

fun error(msg: String) {
    System.err.println(msg)
}

fun error(msg: Any?) = error(msg.toString())

fun error(msg: Any?, t: Throwable) = error(msg.toString() + "\n" + t.fullToString())

fun status(
    unit: String,
    max: Float,
    scale: Float = 1f,
    width: Int = 20
): StatusUpdater {
    return object : StatusUpdater {
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

fun statusPercent(scale: Float = 1f, width: Int = 20): StatusUpdater = status("%", 100f, scale, width)

@Suppress("NOTHING_TO_INLINE")
private inline fun error0(msg: String) = error(msg)

@Suppress("NOTHING_TO_INLINE")
private inline fun warn0(msg: String) = warn(msg)

@Suppress("NOTHING_TO_INLINE")
private inline fun debug0(msg: String) = debug(msg)

@Suppress("NOTHING_TO_INLINE")
private inline fun info0(msg: String) = info(msg)

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
            val message = "[$name] " + if (arguments == null) {
                messagePattern
            } else {
                val format = if (throwable == null) {
                    MessageFormatter.arrayFormat(messagePattern, arguments)
                } else {
                    MessageFormatter.arrayFormat(messagePattern, arguments, throwable)
                }

                if (format.throwable == null) format.message else format.message + "\n" + format.throwable.fullToString()
            }

            when (level) {
                Level.ERROR -> error0(message)
                Level.WARN -> warn0(message)
                Level.DEBUG -> debug0(message)
                else -> info0(message)
            }
        }

        override fun isTraceEnabled() = debugEnabled

        override fun isTraceEnabled(marker: Marker) = debugEnabled

        override fun isDebugEnabled() = debugEnabled

        override fun isDebugEnabled(marker: Marker) = debugEnabled

        override fun isInfoEnabled() = true

        override fun isInfoEnabled(marker: Marker) = true

        override fun isWarnEnabled() = true

        override fun isWarnEnabled(marker: Marker) = true

        override fun isErrorEnabled() = true

        override fun isErrorEnabled(marker: Marker) = true
    }
}