package net.typho.crucible.wrapper

import net.typho.crucible.wrapper.log.debug
import net.typho.misc_util.KtServiceLoader
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.DependencyRequest
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import kotlin.collections.flatten

object CrucibleClassLoader : URLClassLoader(
    "crucible",
    arrayOf(),
    CrucibleClassLoader::class.java.classLoader
) {
    @JvmField
    val artifacts = mutableMapOf<RemoteRepository, MutableList<Artifact>>()

    fun addLibrary(repository: RemoteRepository, artifact: Artifact): Path {
        debug("Loading library ${DependencyName(artifact)}")
        val url = artifact.path.toUri().toURL()
        addURL(url)
        this.artifacts.computeIfAbsent(repository) { mutableListOf() }.add(artifact)
        return artifact.path
    }

    public override fun addURL(url: URL) {
        if (!urLs.contains(url)) {
            super.addURL(url)
        }
    }

    private fun shouldLoad(coords: DependencyName): Boolean {
        val currentVersion = this.artifacts.values.flatten().firstOrNull { artifact ->
            coords.matches(artifact, checkVersion = false)
        }?.version

        if (currentVersion == null) {
            return true
        } else if (currentVersion != coords.version) {
            throw IllegalStateException("Attempting to load plugin '$coords' but already loaded it with version '$currentVersion'")
        } else {
            debug("Skipped loading plugin '$coords' because it has already been loaded")
            return false
        }
    }

    fun load(
        repositories: List<String>,
        artifacts: List<String>
    ): List<Path> = load(repositories, artifacts.map { DependencyName(it) })

    @JvmName("loadArtifacts")
    fun load(
        repositories: List<String>,
        artifacts: List<DependencyName>
    ): List<Path> {
        val repositories = repositories.map { RemoteRepository.Builder(it, "default", it).build() }
        val artifacts = artifacts.toMutableList()
            .filter { shouldLoad(it) }
            .map { Dependency(it.toArtifact(), "runtime") }

        val request = CollectRequest()
        request.repositories = repositories
        request.dependencies = artifacts

        val dependencies = MavenCache.system.collectDependencies(MavenCache.session, request)
        val paths = MavenCache.system.resolveDependencies(MavenCache.session, DependencyRequest(dependencies.root, null))
            .artifactResults
            .map { addLibrary(it.repository as RemoteRepository, it.artifact) }
        // init static code of all plugins in the classpath
        KtServiceLoader.load(Plugin::class.java, initStatic = true, loader = this)
        return paths
    }

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        synchronized(getClassLoadingLock(name)) {
            findLoadedClass(name)?.let { return it }

            val clazz = when {
                name.startsWith("net.typho.crucible.wrapper.") || name.startsWith("org.eclipse.aether.") -> parent.loadClass(name)
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