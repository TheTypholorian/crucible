@file:JvmName("Log")
package net.typho.crucible.wrapper.log

import net.typho.crucible.wrapper.CrucibleWrapper
import net.typho.maven_util.ArtifactCoords
import net.typho.maven_util.DownloadListener
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.net.URI
import java.nio.file.Path

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

object CrucibleDownloadListener : DownloadListener {
    override fun onDownload(
        repository: URI?,
        artifact: ArtifactCoords,
        to: Path
    ) {
        info("Downloading $artifact from $repository")
    }
}