package net.typho.crucible.wrapper

import java.lang.reflect.InvocationTargetException
import kotlin.io.path.Path

object CrucibleWrapper {
    const val KOTLIN_VERSION = "2.4.0"

    @JvmField
    val projectRoot = System.getProperty("crucible.project_root")?.let { Path(it) } ?: Path(System.getProperty("user.dir"))
    @JvmField
    val projectCacheFolder = projectRoot.resolve(".crucible")

    @JvmStatic
    fun main(args: Array<String>) {
        val loader = CrucibleClassLoader
        Thread.currentThread().contextClassLoader = loader

        CrucibleClassLoader.addLibraries(
            listOf(
                "https://repo1.maven.org/maven2",
                "https://typho.net/maven"
            ),
            listOf(
                "org.jetbrains.kotlin:kotlin-stdlib:${KOTLIN_VERSION}",
                "org.jetbrains.kotlin:kotlin-compiler-embeddable:${KOTLIN_VERSION}",
                "org.jetbrains.kotlin:kotlin-scripting-jvm:${KOTLIN_VERSION}",
                "org.jetbrains.kotlin:kotlin-scripting-jvm-host:${KOTLIN_VERSION}",
                "org.jetbrains.kotlin:kotlin-reflect:${KOTLIN_VERSION}",
                "net.typho:crucible.ide_data:1.2.0",
                "net.typho:data_util:1.3.5",
                "net.typho:misc_util:1.0.1"
            )
        )

        try {
            loader.loadClass("net.typho.crucible.Crucible")
                .getMethod("main", Array<String>::class.java)
                .invoke(null, args)
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
    }
}