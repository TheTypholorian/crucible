package net.typho.crucible.wrapper

import java.io.File
import kotlin.io.path.Path

object PropertyManager {
    @JvmField
    val properties = mutableMapOf<String, String>()

    operator fun get(key: String) = properties[key]

    fun getInt(key: String) = get(key)?.toInt()

    fun getFloat(key: String) = get(key)?.toFloat()

    fun getList(key: String) = get(key)?.split(File.pathSeparatorChar)

    fun getPath(key: String) = get(key)?.let { Path(it) }

    fun getPaths(key: String) = getList(key)?.map { Path(it) }
}