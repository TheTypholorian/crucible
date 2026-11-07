package net.typho.crucible.script

import net.typho.crucible.Crucible
import net.typho.crucible.deps.Repositories
import net.typho.crucible.wrapper.CrucibleClassLoader
import net.typho.maven_util.ArtifactCoords
import java.nio.file.Path

@Suppress("unused")
abstract class PluginConfigScript {
    @JvmField
    val repositories = Repositories()

    fun repositories(action: Repositories.() -> Unit) = action(repositories)

    fun plugin(coords: ArtifactCoords) {
        CrucibleClassLoader.addWithDependencies(repositories, listOf(coords))
    }

    fun plugin(
        groupId: String,
        artifactId: String,
        version: String? = null,
        classifier: String? = null,
        extension: String? = null
    ) = plugin(ArtifactCoords(
        groupId,
        artifactId,
        version ?: Crucible.getDependencyVersion(groupId, artifactId),
        classifier,
        extension
    ))

    fun plugin(path: Path, sources: Path? = null) {
        CrucibleClassLoader.addURL(path.toUri().toURL())
        sources?.let { CrucibleClassLoader.sources.add(it) }
    }

    companion object : ProjectScriptType("Crucible Plugin Config", "plugins.crucible.kts", PluginConfigScript::class) {
        init {
            defaultImports.addAll(listOf(
                "net.typho.crucible.*",
                "net.typho.crucible.deps.*",
                "net.typho.crucible.property.*"
            ))
        }
    }
}