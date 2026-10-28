package net.typho.crucible.stonecutter

import org.jetbrains.java.decompiler.api.Decompiler
import org.jetbrains.java.decompiler.main.extern.IContextSource
import org.jetbrains.java.decompiler.main.extern.IResultSaver
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.jar.Manifest

// TODO move out of crucible into the stonecutter plugin once ported
class StonecutterDecompiler(
    @JvmField
    val className: String
) {
    /**
     * Insert earlier versions first
     */
    @JvmField
    val versions = linkedMapOf<String, ByteArray>()

    fun decompile(defaultVersion: String): String {
        if (versions.isEmpty() || !versions.containsKey(defaultVersion)) {
            throw IllegalArgumentException() // TODO
        }

        val sources = versions.mapValues { (version, bytes) -> decompile(className, bytes) }.entries.toList()
        val versionLookup = sources.map { it.key }
        val blocks = diffFiles(sources.map { it.value })

        return buildString {
            for (block in blocks) {
                if (!block.content.isBlank()) {
                    if (block.files.size == versionLookup.size) {
                        append(block.content)
                    } else {
                        val blockVersions = block.files.mapTo(mutableSetOf()) { versionLookup[it] }
                        val range = createVersionRange(
                            versionLookup,
                            blockVersions
                        )
                        val index = block.content.indexOfFirst { !it.isWhitespace() }
                        val indent = if (index == -1) "" else block.content.substring(0, index)
                        append("$indent//? if $range {\n")

                        if (blockVersions.contains(defaultVersion)) {
                            append(block.content)
                            append("\n")
                        } else {
                            append(wrapTrimmed(block.content, "/*", "\n*/"))
                        }

                        append("$indent//? }")
                    }
                }

                append('\n')
            }
        }
    }

    companion object {
        @JvmStatic
        fun wrapTrimmed(input: String, start: String, end: String): String {
            val leading = input.takeWhile { it.isWhitespace() }
            val trailing = input.takeLastWhile { it.isWhitespace() }
            val content = input.trim()

            return "$leading$start$content$end$trailing"
        }

        @JvmStatic
        fun createVersionRange(
            allVersions: List<String>,
            validVersions: Set<String>
        ): String {
            val ranges = mutableListOf<String>()
            var start = -1

            for (index in allVersions.indices) {
                val isValid = allVersions[index] in validVersions

                if (isValid && start == -1) {
                    start = index
                }

                if (start != -1 && (!isValid || index == allVersions.lastIndex)) {
                    val end = if (isValid && index == allVersions.lastIndex) index else index - 1
                    val range = when {
                        start == 0 && end == allVersions.lastIndex -> "*"
                        start == 0 -> "<=${allVersions[end]}"
                        end == allVersions.lastIndex -> ">=${allVersions[start]}"
                        start == end -> allVersions[start]
                        else -> ">=${allVersions[start]} <=${allVersions[end]}"
                    }

                    ranges.add(range)
                    start = -1
                }
            }

            return ranges.joinToString(" || ")
        }

        data class Block(
            @JvmField
            val content: String,
            @JvmField
            val files: Set<Int>
        )

        @JvmStatic
        fun diffFiles(columns: Map<Int, List<String>>): List<Block> {
            if (columns.isEmpty()) {
                return listOf()
            }

            val columns = columns.mapValuesTo(mutableMapOf()) { it.value.toMutableList() }
            val blocks = mutableListOf<Block>()

            while (true) {
                columns.entries.removeIf { it.value.isEmpty() }

                if (columns.isEmpty()) {
                    break
                }

                val row = columns.map { it.value.first() }
                val check = row.first()

                if (row.all { it == check }) {
                    blocks.add(Block(check, columns.keys.toSet()))
                    columns.forEach { it.value.removeFirstOrNull() }
                } else {
                    val excess = mutableMapOf<Int, List<String>>()
                    val indices = columns.mapValues { it.value.indexOf(check) }.filter { it.value != -1 }
                    indices.forEach { (file, index) ->
                        val column = columns[file]!!

                        if (index > 0) {
                            excess[file] = column.filterIndexed { i, string -> i < index }
                        }
                    }

                    if (excess.any { it.value.isNotEmpty() }) {
                        blocks.addAll(diffFiles(excess))
                    }

                    blocks.add(Block(check, indices.keys))
                    indices.forEach { (file, index) ->
                        repeat(index + 1) { columns[file]!!.removeFirst() }
                    }
                }
            }

            return blocks
        }

        @JvmStatic
        fun diffFiles(files: List<String>): List<Block> {
            return diffFiles(files.mapIndexed { index, text -> index to text }.associate { (index, text) -> index to text.lines() })

            /*
            if (files.isEmpty()) {
                return emptyList()
            }

            val baseLines = files.first().lines()
            val baseFiles = baseLines.map { mutableListOf(0) }
            val prefixMap = mutableMapOf<Int, MutableList<Block>>()
            val extraLines = mutableListOf<Block>()

            files.forEachIndexed { fileIndex, file ->
                val lines = file.lines()
                val baseSize = baseLines.size
                val lineSize = lines.size
                val table = Array(baseSize + 1) { IntArray(lineSize + 1) } // TODO this is slow but like it's fine for now cus it's cached :aga:

                for (i in baseSize - 1 downTo 0) {
                    for (j in lineSize - 1 downTo 0) {
                        table[i][j] = if (baseLines[i] == lines[j]) {
                            table[i + 1][j + 1] + 1
                        } else {
                            maxOf(table[i + 1][j], table[i][j + 1])
                        }
                    }
                }

                val matched = mutableListOf<Pair<Int, Int>>()
                var basePos = 0
                var linePos = 0

                while (basePos < baseSize && linePos < lineSize) {
                    if (baseLines[basePos] == lines[linePos]) {
                        matched.add(basePos to linePos)
                        basePos++
                        linePos++
                    } else if (table[basePos][linePos + 1] >= table[basePos + 1][linePos]) {
                        linePos++
                    } else {
                        basePos++
                    }
                }

                for ((baseIndex, _) in matched) {
                    baseFiles[baseIndex].add(fileIndex)
                }

                val matchedLines = matched.associate { it.second to it.first }
                var targetIndex = baseSize

                for (i in lines.indices.reversed()) {
                    val baseIndex = matchedLines[i]

                    if (baseIndex != null) {
                        targetIndex = baseIndex
                    } else {
                        val block = Block(lines[i], setOf(fileIndex))

                        if (targetIndex < baseSize) {
                            prefixMap.getOrPut(targetIndex) { mutableListOf() }.add(0, block)
                        } else {
                            extraLines.add(0, block)
                        }
                    }
                }
            }

            val result = mutableListOf<Block>()

            for (i in baseLines.indices) {
                result.addAll(prefixMap[i] ?: emptyList())
                result.add(Block(baseLines[i], baseFiles[i].toSet()))
            }

            result.addAll(extraLines)

            return buildList {
                var temp: Block? = null

                fun flush() {
                    temp?.let { add(it) }
                    temp = null
                }

                for (block in result) {
                    if (block.content.isEmpty()) {
                        flush()
                        add(block)
                    } else if (temp == null) {
                        temp = block
                    } else if (temp!!.versions == block.versions) {
                        temp = Block(temp!!.content + "\n" + block.content, temp!!.versions)
                    } else {
                        flush()
                        temp = block
                    }
                }

                flush()
            }
             */
        }

        @JvmStatic
        fun decompile(className: String, bytes: ByteArray): String {
            lateinit var result: String

            Decompiler.Builder()
                .inputs(object : IContextSource {
                    override fun getName(): String = "stonecutter"

                    override fun getEntries() = IContextSource.Entries(
                        listOf(IContextSource.Entry.atBase(className)),
                        emptyList(),
                        emptyList()
                    )

                    override fun getInputStream(resource: String): InputStream? {
                        println("get $resource")
                        return if (resource == "$className.class") {
                            ByteArrayInputStream(bytes)
                        } else {
                            null
                        }
                    }

                    override fun createOutputSink(saver: IResultSaver) = object : IContextSource.IOutputSink {
                        override fun begin() = Unit

                        override fun acceptClass(
                            qualifiedName: String,
                            fileName: String,
                            content: String,
                            mapping: IntArray?
                        ) {
                            result = content
                        }

                        override fun acceptDirectory(directory: String) = Unit

                        override fun acceptOther(path: String) = Unit

                        override fun close() = Unit
                    }
                })
                .output(object : IResultSaver {
                    override fun saveFolder(path: String) = Unit

                    override fun copyFile(source: String, path: String, entryName: String) = Unit

                    override fun saveClassFile(
                        path: String,
                        qualifiedName: String,
                        entryName: String,
                        content: String,
                        mapping: IntArray?
                    ) = Unit

                    override fun createArchive(path: String, archiveName: String, manifest: Manifest?) = Unit

                    override fun saveDirEntry(path: String, archiveName: String, entryName: String) = Unit

                    override fun copyEntry(
                        source: String,
                        path: String,
                        archiveName: String,
                        entry: String
                    ) = Unit

                    override fun saveClassEntry(
                        path: String,
                        archiveName: String,
                        qualifiedName: String?,
                        entryName: String,
                        content: String?
                    ) = Unit

                    override fun closeArchive(path: String, archiveName: String) = Unit
                })
                .build()
                .decompile()

            return result
        }
    }
}