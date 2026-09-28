package net.typho.crucible

import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services

object Crucible {
    @JvmStatic
    fun main(args: Array<String>) {
        val compiler = K2JVMCompiler()

        val args = compiler.createArguments().apply {
            freeArgs = listOf("test/Test.kt")
            destination = "test/out"
            jvmTarget = "21"
        }
        val messages = PrintingMessageCollector(
            System.err,
            MessageRenderer.PLAIN_FULL_PATHS,
            true
        )
        val result = compiler.exec(
            messages,
            Services.EMPTY,
            args
        )
        println("Compilation result: $result")
    }
}