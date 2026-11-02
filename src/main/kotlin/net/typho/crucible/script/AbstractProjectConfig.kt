package net.typho.crucible.script

import net.typho.crucible.plugins.Plugins
import net.typho.crucible.property.PropertyContainer
import net.typho.crucible.wrapper.CrucibleClassLoader
import net.typho.crucible.wrapper.log.error
import net.typho.maven_util.ArtifactCoords
import java.io.File
import java.net.URI
import kotlin.collections.mapTo
import kotlin.io.path.toPath
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.*

abstract class AbstractProjectConfig {
    companion object : PropertyContainer() {
        @JvmField
        val defaultImports = listProperty(
            "net.typho.crucible.*",
            "net.typho.crucible.Crucible.*",
            "net.typho.crucible.wrapper.CrucibleWrapper.*",
            "net.typho.crucible.deps.*",
            "net.typho.crucible.plugins.*",
            "net.typho.crucible.property.*",
            "net.typho.crucible.source.*",
            "net.typho.crucible.task.*",
            "net.typho.crucible.task.Task.*",
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
                    dependenciesSources.append(JvmDependency(CrucibleClassLoader.sources.map { it.artifact.toFile() }))
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
                        CrucibleClassLoader.addWithDependencies(anno.repositories.map { URI(it) }, anno.plugins.map { ArtifactCoords(it) }).mapTo(plugins) { it.artifact.toFile() }
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