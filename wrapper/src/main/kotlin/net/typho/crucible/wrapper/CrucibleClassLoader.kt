package net.typho.crucible.wrapper

import net.typho.crucible.wrapper.log.CrucibleDownloadListener
import net.typho.crucible.wrapper.log.debug
import net.typho.maven_util.ArtifactCoords
import net.typho.maven_util.ArtifactPointer
import net.typho.maven_util.MavenUtil
import net.typho.misc_util.KtServiceLoader
import java.net.URI
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path

object CrucibleClassLoader : URLClassLoader(
    "crucible",
    arrayOf(),
    CrucibleClassLoader::class.java.classLoader
) {
    @JvmField
    val sources = mutableListOf<ArtifactPointer<Path>>()

    fun addLibrary(repositories: List<URI>, coords: ArtifactCoords): ArtifactPointer<Path> {
        debug("Loading library $coords")

        val pointer = MavenUtil.cachedLookup(
            coords,
            CrucibleWrapper.mavenCacheFolder,
            repositories,
            CrucibleDownloadListener
        ).orThrow()
        addURL(pointer.artifact.toUri().toURL())

        MavenUtil.cachedLookup(
            coords.copy(classifier = "sources"),
            CrucibleWrapper.mavenCacheFolder,
            pointer.repository?.let { listOf(it) } ?: repositories,
            CrucibleDownloadListener
        ).value?.let { sources.add(it) }

        return pointer
    }

    fun addLibrary(repositories: List<URI>, coords: ArtifactPointer<ArtifactCoords>): ArtifactPointer<Path> {
        val pointer = coords.repository?.let {
            MavenUtil.cachedLookup(
                coords.artifact,
                CrucibleWrapper.mavenCacheFolder,
                listOf(it),
                CrucibleDownloadListener
            ).value
        } ?: MavenUtil.cachedLookup(
            coords.artifact,
            CrucibleWrapper.mavenCacheFolder,
            repositories,
            CrucibleDownloadListener
        ).orThrow()
        addURL(pointer.artifact.toUri().toURL())

        MavenUtil.cachedLookup(
            coords.artifact.copy(classifier = "sources"),
            CrucibleWrapper.mavenCacheFolder,
            pointer.repository?.let { listOf(it) } ?: coords.repository?.let { listOf(it) } ?: repositories,
            CrucibleDownloadListener
        ).value?.let { sources.add(it) }

        return pointer
    }

    public override fun addURL(url: URL) {
        if (!urLs.contains(url)) {
            super.addURL(url)
        }
    }

    fun load(
        repositories: List<URI>,
        artifacts: List<ArtifactCoords>
    ): List<ArtifactPointer<Path>> {
        val dependencies = MavenUtil.cachedLookupDependencies(
            artifacts,
            CrucibleWrapper.mavenCacheFolder,
            repositories,
            CrucibleDownloadListener
        ).orThrow()
        val paths = dependencies.map { addLibrary(repositories, it) }
        // init static code of all plugins in the classpath
        KtServiceLoader.load(Plugin::class.java, initStatic = true, loader = this)
        return paths
    }

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        synchronized(getClassLoadingLock(name)) {
            findLoadedClass(name)?.let { return it }

            val clazz = when {
                name.startsWith("net.typho.crucible.wrapper.") || name.startsWith("net.typho.maven_util.") -> parent.loadClass(name)
                name.startsWith("java.") -> getPlatformClassLoader().loadClass(name)
                else -> try {
                    findClass(name)
                } catch (_: ClassNotFoundException) {
                    getPlatformClassLoader().loadClass(name)
                }
            }

            if (resolve) {
                resolveClass(clazz)
            }

            return clazz
        }
    }
}