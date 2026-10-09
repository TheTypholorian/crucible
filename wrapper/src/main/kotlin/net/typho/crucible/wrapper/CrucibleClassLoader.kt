package net.typho.crucible.wrapper

import net.typho.crucible.wrapper.log.debug
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.DependencyRequest
import java.net.URL
import java.net.URLClassLoader
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream

object CrucibleClassLoader : URLClassLoader(
    "crucible",
    arrayOf(CrucibleClassLoader::class.java.classLoader.getResourceAsStream("crucible.jar")!!.use { input ->
        val path = CrucibleWrapper.projectCacheFolder.resolve("internal").resolve("crucible.jar")
        path.parent.createDirectories()
        path.outputStream().use { input.copyTo(it) }
        path.toUri().toURL()
    }),
    CrucibleClassLoader::class.java.classLoader
) {
    public override fun addURL(url: URL) = super.addURL(url)

    fun addLibraries(repositories: List<String>, artifacts: List<String>) {
        val request = CollectRequest()
        request.repositories = repositories.map { RemoteRepository.Builder(it, "default", it).build() }
        request.dependencies = artifacts.map { Dependency(DefaultArtifact(it), "runtime") }

        val dependencies = MavenCache.system.collectDependencies(MavenCache.session, request)
        MavenCache.system.resolveDependencies(MavenCache.session, DependencyRequest(dependencies.root, null))
            .artifactResults
            .forEach {
                debug("Loading library ${it.artifact.groupId}:${it.artifact.artifactId}:${it.artifact.version}")
                addURL(it.artifact.path.toUri().toURL())
            }
    }

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        synchronized(getClassLoadingLock(name)) {
            findLoadedClass(name)?.let { return it }

            val clazz = when {
                name.startsWith("net.typho.crucible.wrapper.") -> parent.loadClass(name)
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