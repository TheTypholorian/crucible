package net.typho.crucible

import net.typho.crucible.deps.DependencyCoordinates
import net.typho.crucible.deps.DependencyFinder

object Crucible {
    @JvmStatic
    fun main(args: Array<String>) {
        DependencyFinder.Maven("https://typho.net/maven", DependencyFinder.MAVEN_CENTRAL).use {
            println(it.find(DependencyCoordinates("net.typho:data_util:1.3.4")))
            println(it.find(DependencyCoordinates("net.typho:typho_publish:1.0.3")))
            println(it.find(DependencyCoordinates("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.4.0")))
        }

        /*
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
         */
    }
}