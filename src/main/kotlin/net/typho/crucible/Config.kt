package net.typho.crucible

import java.nio.file.Path

object Config {
    @JvmField
    val GLOBAL_FOLDER = System.getProperty("crucible.global_folder")?.let { Path.of(it) } ?: Path.of(System.getProperty("user.home")).resolve(".crucible")
    @JvmField
    val CACHE_FOLDER = GLOBAL_FOLDER.resolve("caches")
    @JvmField
    val MAVEN_CACHE_FOLDER = CACHE_FOLDER.resolve("maven")
    @JvmField
    val LOG_IMPL_CLASS: String? = System.getProperty("crucible.log_impl")
}