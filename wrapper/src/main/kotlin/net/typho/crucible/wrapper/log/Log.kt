@file:JvmName("Log")
package net.typho.crucible.wrapper.log

import com.sun.tools.javac.code.TypeAnnotationPosition.field
import net.typho.crucible.wrapper.CrucibleWrapper
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

fun Throwable.fullToString(): String {
    val out = ByteArrayOutputStream()
    val writer = PrintStream(out, false)
    printStackTrace(writer)
    out.flush()
    return String(out.toByteArray())
}

fun info(msg: String) {
    println(msg)
}

fun info(msg: Any?) = info(msg.toString())

fun warn(msg: String) {
    info("\u001b[93m$msg\u001b[0m")
}

fun warn(msg: Any?) = warn(msg.toString())

fun debug(msg: String) {
    if (CrucibleWrapper.debug) {
        info(msg)
    }
}

fun debug(msg: Any?) = debug(msg.toString())

fun error(msg: String) {
    System.err.println(msg)
}

fun error(msg: Any?) = error(msg.toString())

fun error(t: Throwable) = error(t.fullToString())

fun error(msg: Any?, t: Throwable) = error(msg.toString() + "\n" + t.fullToString())

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

            RuntimeException().printStackTrace()

            when (level) {
                Level.ERROR -> error0(message)
                Level.WARN -> warn0(message)
                Level.DEBUG -> debug0(message)
                else -> info0(message)
            }
        }

        override fun isTraceEnabled() = CrucibleWrapper.debug

        override fun isTraceEnabled(marker: Marker) = CrucibleWrapper.debug

        override fun isDebugEnabled() = CrucibleWrapper.debug

        override fun isDebugEnabled(marker: Marker) = CrucibleWrapper.debug

        override fun isInfoEnabled() = true

        override fun isInfoEnabled(marker: Marker) = true

        override fun isWarnEnabled() = true

        override fun isWarnEnabled(marker: Marker) = true

        override fun isErrorEnabled() = true

        override fun isErrorEnabled(marker: Marker) = true
    }
}