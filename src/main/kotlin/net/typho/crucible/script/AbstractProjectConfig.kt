package net.typho.crucible.script

import net.typho.crucible.Crucible
import net.typho.crucible.deps.Dependencies
import net.typho.crucible.deps.Repositories
import net.typho.crucible.plugins.Plugins
import net.typho.crucible.property.ListProperty
import net.typho.crucible.task.Task
import net.typho.crucible.wrapper.CrucibleClassLoader
import net.typho.crucible.wrapper.DependencyName
import net.typho.crucible.wrapper.MavenCache
import net.typho.crucible.wrapper.log.error
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.resolution.ArtifactResolutionException
import org.eclipse.aether.transfer.ArtifactNotFoundException
import java.io.File
import kotlin.io.path.toPath
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.*

abstract class AbstractProjectConfig {
    fun repositories(action: Repositories.() -> Unit) = action(Crucible.repositories)

    fun dependencies(action: Dependencies.() -> Unit) = action(Crucible.dependencies)

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
        val defaultImports = ListProperty(
            "net.typho.crucible.*",
            "net.typho.crucible.Crucible.*",
            "net.typho.crucible.deps.*",
            "net.typho.crucible.plugins.*",
            "net.typho.crucible.property.*",
            "net.typho.crucible.source.*",
            "net.typho.crucible.task.*",
            "net.typho.crucible.ide.data.*"
        ).finalizeOnRead()

        @JvmStatic
        fun createCompilationConfig(includeSources: Boolean = false) = ScriptCompilationConfiguration {
            displayName("Crucible Project Config")
            fileExtension("kts")
            filePathPattern("(.*/)?([^/]*\\.)?crucible\\.kts")
            baseClass(KotlinType(AbstractProjectConfig::class))
            defaultImports.append(AbstractProjectConfig.defaultImports())

            jvm {
                jvmTarget("21")
                dependencies.append(JvmDependency(CrucibleClassLoader.urLs.map { it.toURI().toPath().toFile() }))
            }

            ide {
                acceptedLocations(ScriptAcceptedLocation.Project)

                if (includeSources) {
                    dependenciesSources.append(JvmDependency(CrucibleClassLoader.artifacts.flatMap { (repository, artifacts) ->
                        artifacts.mapNotNull { artifact ->
                            try {
                                MavenCache.system.resolveArtifact(MavenCache.session, ArtifactRequest(DependencyName(artifact).toArtifact(classifier = "sources"), listOf(repository), null)).artifact.path.toFile()
                            } catch (e: ArtifactResolutionException) {
                                if (e.result.exceptions.all { it is ArtifactNotFoundException }) {
                                    null
                                } else {
                                    throw e
                                }
                            }
                        }
                    }))
                }
            }

            refineConfiguration {
                onAnnotations(Plugins::class, handler = ::processPlugins)
            }
        }

        @JvmStatic
        fun processPlugins(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
            try {
                val plugins = mutableListOf<File>()

                context.collectedData
                    ?.get(ScriptCollectedData.collectedAnnotations)
                    ?.mapNotNull { anno -> anno.annotation as? Plugins }
                    ?.forEach { anno ->
                        CrucibleClassLoader.load(anno.repositories.asList(), anno.plugins.asList()).mapTo(plugins) { it.toFile() }
                    }

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