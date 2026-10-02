package net.typho.crucible.source

import net.typho.crucible.Config
import net.typho.crucible.task.Task
import java.io.File
import java.nio.file.Path
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.io.path.relativeTo
import kotlin.io.path.walk

open class JarTask(
    inputs: () -> List<Path>,
    output: () -> Path
) : Task<Unit> {
    val inputs by Config.finalizeOnRead(inputs)
    val output by Config.finalizeOnRead(output)

    override fun invoke() {
        output.parent.createDirectories()
        JarOutputStream(output.outputStream()).use { jar ->
            inputs.forEach { input ->
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
            }
        }
    }

    object Main : JarTask(CompileTask, {
        Config.jarOutputFolder.resolve(buildString {
            append(Config.projectName)

            if (Config.projectVersion.isNotEmpty()) {
                append('-')
                append(Config.projectVersion)
            }

            append(".jar")
        })
    })
}