package net.typho.crucible.wrapper

import net.typho.crucible.wrapper.log.CrucibleDownloadListener
import net.typho.crucible.wrapper.log.debug
import net.typho.crucible.wrapper.log.warn
import net.typho.maven_util.ArtifactCoords
import net.typho.maven_util.ArtifactPointer
import net.typho.maven_util.LookupResult
import net.typho.maven_util.MavenUtil
import java.net.URI
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.ServiceLoader

object CrucibleClassLoader : URLClassLoader(
    "crucible",
    arrayOf(),
    CrucibleClassLoader::class.java.classLoader
) {
    @JvmField
    val sources = mutableListOf<ArtifactPointer<Path>>()

    fun addSources(repositories: List<URI>, coords: ArtifactCoords): LookupResult<ArtifactPointer<Path>> {
        val pointer = MavenUtil.cachedLookup(
            coords.copy(classifier = "sources"),
            CrucibleWrapper.mavenCacheFolder,
            repositories,
            CrucibleDownloadListener
        )
        pointer.value?.let { sources.add(it) } ?: warn("Sources for $coords not found")
        return pointer
    }

    fun addLibrary(repositories: List<URI>, coords: ArtifactCoords): ArtifactPointer<Path> {
        val pointer = MavenUtil.cachedLookup(
            coords,
            CrucibleWrapper.mavenCacheFolder,
            repositories,
            CrucibleDownloadListener
        ).orThrow()
        addURL(pointer.artifact.toUri().toURL())

        addSources(repositories, coords)

        return pointer
    }

    public override fun addURL(url: URL) {
        if (!urLs.contains(url)) {
            super.addURL(url)
        }
    }

    fun addWithDependencies(
        repositories: List<URI>,
        artifacts: List<ArtifactCoords>
    ): List<ArtifactPointer<Path>> {
        val dependencies = MavenUtil.cachedLookupDependencies(
            artifacts,
            CrucibleWrapper.mavenCacheFolder,
            repositories,
            CrucibleDownloadListener
        ).orThrow()
        val paths = dependencies.map { addLibrary(repositories, it.artifact) }

        ServiceLoader.load(Plugin::class.java, this)
            .stream()
            .map { it.type() }
            .forEach { Class.forName(it.name, true, this) }

        return paths
    }

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        synchronized(getClassLoadingLock(name)) {
            findLoadedClass(name)?.let { return it }

            val cls = try {
                parent.loadClass(name)
            } catch (_: ClassNotFoundException) {
                try {
                    findClass(name)
                } catch (_: ClassNotFoundException) {
                    getPlatformClassLoader().loadClass(name)
                }
            }

            if (resolve) {
                resolveClass(cls)
            }

            return cls
        }
    }
}