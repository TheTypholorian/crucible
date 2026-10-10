package net.typho.crucible.script

import net.typho.crucible.Crucible
import net.typho.crucible.deps.Dependencies
import net.typho.crucible.deps.Repositories
import net.typho.crucible.plugins.Plugins
import net.typho.crucible.task.Task
import net.typho.crucible.wrapper.CrucibleClassLoader
import net.typho.crucible.wrapper.log.error
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.*

abstract class AbstractProjectConfig {
    val repositories by Crucible::repositories
    val dependencies by Crucible::dependencies

    var group by Crucible.group
    var name by Crucible.name
    var version by Crucible.version

    var javaVersion by Crucible.javaVersion

    fun repositories(action: Repositories.() -> Unit) = action(repositories)

    fun dependencies(action: Dependencies.() -> Unit) = action(dependencies)

    fun getTask(name: String) = Task.get(name)

    fun <T : Task<*>> getTask(name: String, type: Class<T>) = Task.get(name, type)

    fun <T : Task<*>> registerTask(name: String, task: T): T {
        Task.all.put(name, task)?.let { old ->
            throw IllegalArgumentException("Task $old is already registered under the name '$name' (tried to register $task)")
        }

        return task
    }

    fun <T : Task<*>> registerTask(name: String, task: Class<T>, config: T.() -> Unit): T {
        val task = task.getConstructor().newInstance()
        config(task)
        return registerTask(name, task)
    }

    inline fun <reified T : Task<*>> registerTask(name: String, noinline config: T.() -> Unit): T {
        return registerTask(name, T::class.java, config)
    }

    companion object {
        @JvmField
        val compilationConfig = ScriptCompilationConfiguration {
            displayName("Crucible Project Config")
            fileExtension("kts")
            filePathPattern("(.*/)?([^/]*\\.)?crucible\\.kts")
            baseClass(KotlinType(AbstractProjectConfig::class))
            defaultImports.append("net.typho.crucible.*", "net.typho.crucible.deps.*", "net.typho.crucible.plugins.*", "net.typho.crucible.property.*", "net.typho.crucible.source.*", "net.typho.crucible.task.*")

            jvm {
                jvmTarget("21")
                //dependenciesFromCurrentContext(wholeClasspath = true)
                dependenciesFromClassloader(classLoader = CrucibleClassLoader, wholeClasspath = true, unpackJarCollections = true)
            }

            ide {
                acceptedLocations(ScriptAcceptedLocation.Project)
            }

            refineConfiguration {
                onAnnotations(Plugins::class, handler = ::processPlugins)
            }
        }

        @JvmStatic
        fun processPlugins(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
            try {
                val plugins = context.collectedData
                    ?.get(ScriptCollectedData.collectedAnnotations)
                    ?.map { it.annotation }
                    ?.filterIsInstance<Plugins>()
                    ?.flatMap { CrucibleClassLoader.addLibraries(it.repositories.asList(), it.plugins.asList()) }
                    ?.map { it.toFile() }

                return context.compilationConfiguration
                    .with { updateClasspath(plugins) }
                    .asSuccess()
            } catch (e: Throwable) {
                error("Script plugin resolution error", e)
                return ResultWithDiagnostics.Failure(e.asDiagnostics())
            }
        }
    }
}