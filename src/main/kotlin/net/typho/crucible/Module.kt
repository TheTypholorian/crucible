package net.typho.crucible

import net.typho.crucible.deps.Dependencies
import net.typho.crucible.ide.data.DependencyPathType
import net.typho.crucible.ide.data.IDEContentRoot
import net.typho.crucible.ide.data.IDEModule
import net.typho.crucible.property.PropertyContainer
import net.typho.crucible.source.CompileTask
import net.typho.crucible.source.JarTask
import net.typho.crucible.source.JavaExecTask
import net.typho.crucible.source.JavaSourceType
import net.typho.crucible.source.KotlinSourceType
import net.typho.crucible.source.ResourcesSourceType
import net.typho.crucible.source.SourceType
import net.typho.crucible.task.Task
import net.typho.misc_util.EventGraph
import kotlin.io.path.absolutePathString

class Module(
    override val id: String
) : PropertyContainer(), Named {
    val buildFolder by lazyProperty { Crucible.buildFolder().resolve(id) }.finalizeOnRead()
    val sourceInputFolder by lazyProperty { Crucible.sourceInputFolder().resolve(id) }.finalizeOnRead()
    val jarOutputFolder by lazyProperty { buildFolder().resolve("jars") }.finalizeOnRead()
    val sourceOutputFolder by lazyProperty { buildFolder().resolve("src") }.finalizeOnRead()

    @JvmField
    val sourceTypes = EventGraph<String, SourceType>()
    @JvmField
    val dependencies = Dependencies(Crucible.repositories, Crucible.fallbackDependencyVersions /* TODO */)
    lateinit var compileTask: CompileTask

    fun dependencies(action: Dependencies.() -> Unit) = action(dependencies)

    fun toIDE() = IDEModule(
        id,
        Crucible.projectRoot.absolutePathString(),//sourceInputFolder().absolutePathString(),
        sourceTypes.resolve().flatMap { type -> type.event.inputs().map { IDEContentRoot(type.event.type, false, it.absolutePathString()) } },
        dependencies
    )

    fun taskId(id: String) = if (this.id == "main") id else this.id + id.replaceFirstChar { it.uppercase() }

    fun kotlin() {
        sourceTypes.register(KotlinSourceType(this))
    }

    fun java() {
        sourceTypes.register(JavaSourceType(this))
    }

    fun resources() {
        sourceTypes.register(ResourcesSourceType(this))
    }

    fun compileTask() = Task.registerTask(taskId("compile"), CompileTask()) {
        sourceTypes.addAll(this@Module.sourceTypes.resolve().map { it.event })
        compileTask = this
    }

    fun jarTask() = Task.registerTask(taskId("jar"), JarTask()) {
        inputs.addAll(compileTask)
        output.setLazy {
            jarOutputFolder().resolve(buildString {
                append(Crucible.name())

                if (Crucible.version().isNotEmpty()) {
                    append('-')
                    append(Crucible.version())
                }

                if (this@Module.id != "main") {
                    append('-')
                    append(this@Module.id)
                }

                append(".jar")
            })
        }
    }

    @JvmOverloads
    fun runTask(
        mainClass: String,
        javaArgs: Map<String, String> = mapOf(),
        vararg programArgs: String
    ) = Task.registerTask(taskId("run"), JavaExecTask()) {
        classpath.addAll(compileTask)
        classpath.addAll { dependencies.getPaths(DependencyPathType.BINARY) }
        this.mainClass.set(mainClass)
        this.javaArgs.addAll(javaArgs.map { (key, value) -> key to value })
        this.programArgs.addAll(listOf(*programArgs))
    }
}