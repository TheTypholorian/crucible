package net.typho.crucible

import java.net.URL
import java.net.URLClassLoader

open class LibraryClassLoader(
    urls: Array<URL>,
    parent: ClassLoader?
) : URLClassLoader(urls, parent) {
}