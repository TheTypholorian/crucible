@file:JvmName("Log")
package net.typho.crucible

import net.typho.misc_util.KtServiceLoader
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.text.NumberFormat
import kotlin.math.floor

@JvmField
@get:JvmName("INSTANCE")
val LOG = Config.LOG_IMPL_CLASS?.let { KtServiceLoader.load(ILog::class.java, listOf(it)).single().get() } ?: LogImpl

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

    fun error(msg: String)

    fun error(msg: Any?) = info(msg.toString())

    fun error(msg: Any?, t: Throwable) = info(msg.toString() + "\n" + t.fullToString())

    fun status(unit: String, max: Float, scale: Float = 1f, width: Int = 20): StatusUpdater

    fun statusPercent(scale: Float = 1f, width: Int = 20): StatusUpdater = status("%", 100f, scale, width)
}

private object LogImpl : ILog {
    override fun info(msg: String) {
        println(msg)
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