package net.typho.crucible.source

import net.typho.crucible.Crucible
import net.typho.crucible.property.ListProperty
import net.typho.crucible.property.Property
import net.typho.crucible.task.Task
import java.io.File
import java.nio.file.Path
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.outputStream
import kotlin.io.path.relativeTo
import kotlin.io.path.walk

open class JarTask : Task<Unit>() {
    override val group: String
        get() = "build"

    @JvmField
    val inputs = ListProperty<Path>().finalizeOnRead()
    @JvmField
    val output = Property<Path>().finalizeOnRead()

    override fun run() {
        val output = output()

        output.parent.createDirectories()
        JarOutputStream(output.outputStream()).use { jar ->
            inputs.forEach { input ->
                if (input.isDirectory()) {
                    input.walk().forEach { path ->
                        if (path != input) {
                            val entry = path.relativeTo(input).toString().replace(File.separatorChar, '/')

                            if (path.toFile().isDirectory) {
                                jar.putNextEntry(ZipEntry("$entry/"))
                                jar.closeEntry()
                            } else {
                                jar.putNextEntry(ZipEntry(entry))
                                path.inputStream().use { it.transferTo(jar) }
                                jar.closeEntry()
                            }
                        }
                    }
                } else {
                    jar.putNextEntry(ZipEntry(input.name))
                    input.inputStream().use { it.transferTo(jar) }
                    jar.closeEntry()
                }
            }
        }
    }

    object Main : JarTask() {
        override val description: String
            get() = "Default jar task"

        init {
            inputs.addAll(CompileTask.Main)
            output.setLazy {
                Crucible.jarOutputFolder().resolve(buildString {
                    append(Crucible.name())

                    if (Crucible.version().isNotEmpty()) {
                        append('-')
                        append(Crucible.version())
                    }

                    append(".jar")
                })
            }
        }
    }
}