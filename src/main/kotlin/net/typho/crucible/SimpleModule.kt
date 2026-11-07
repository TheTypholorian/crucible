package net.typho.crucible

import net.typho.crucible.deps.Dependencies
import net.typho.crucible.deps.Repositories
import net.typho.crucible.ide_data.IDEContentRootData
import net.typho.crucible.ide_data.IDELibraryDependencyData
import net.typho.crucible.ide_data.IDELibraryLevel
import net.typho.crucible.ide_data.IDELibraryPathType
import net.typho.crucible.ide_data.IDEModuleData
import net.typho.crucible.source.CompileTask
import net.typho.crucible.source.JarTask
import net.typho.crucible.source.JavaExecTask
import net.typho.crucible.source.JavaSourceType
import net.typho.crucible.source.KotlinSourceType
import net.typho.crucible.source.ResourcesSourceType
import net.typho.crucible.source.SourceType
import net.typho.crucible.task.Task
import net.typho.crucible.wrapper.CrucibleWrapper
import net.typho.misc_util.EventGraph
import java.nio.file.Path
import kotlin.collections.map

class SimpleModule(
    override val id: String
) : Module() {
    @JvmField
    val sourceTypes = EventGraph<String, SourceType>()
    @JvmField
    val repositories = Repositories()
    @JvmField
    val dependencies = Dependencies(repositories)
    lateinit var compileTask: CompileTask
    val shallowIDEData by lazyProperty(IDEModuleData.CODEC) {
        IDEModuleData(
            externalName = id,
            id = id,
            moduleTypeId = "GENERAL_MODULE",
            externalConfigPath = CrucibleWrapper.projectRoot,
            moduleFileDirectoryPath = sourceInputFolder()
        )
    }

    fun repositories(action: Repositories.() -> Unit) = action(repositories)

    fun dependencies(action: Dependencies.() -> Unit) = action(dependencies)

    override fun toIDE() = shallowIDEData().copy(
        children = sourceTypes.resolve().flatMap { type ->
            type.event.inputs().map {
                IDEContentRootData(
                    mapOf(type.event.type to listOf(
                        IDEContentRootData.SourceRoot(it, null)
                    )),
                    it,
                    listOf()
                )
            }
        } + dependencies.map { library ->
            // TODO dependency scopes
            // TODO module dependencies
            IDELibraryDependencyData(
                ownerModule = shallowIDEData(),
                target = library,
                level = IDELibraryLevel.MODULE
            )
        }
    )

    override fun getClasspath(): List<Path> {
        return dependencies.getPaths(IDELibraryPathType.BINARY)
    }

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
        sourceTypes.addAll(this@SimpleModule.sourceTypes.resolve().map { it.event })
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

                if (this@SimpleModule.id != "main") {
                    append('-')
                    append(this@SimpleModule.id)
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
        classpath.addAll { dependencies.getPaths(IDELibraryPathType.BINARY) }
        this.mainClass.set(mainClass)
        this.javaArgs.addAll(javaArgs.map { (key, value) -> key to value })
        this.programArgs.addAll(listOf(*programArgs))
    }
}