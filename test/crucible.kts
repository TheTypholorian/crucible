import net.typho.crucible.stonecutter.StonecutterDecompiler

/*
repositories {
    mavenCentral()
    typhoNet()
}

val module = modules.register(name()) {
    sourceInputFolder.set(sourceInputFolder())

    dependencies {
        kotlin("stdlib")
        add("net.typho:data_util")
        add("net.typho:asm_util")
    }

    java()
    resources()
    kotlin()

    compileTask()
    jarTask()
    runTask(mainClass = "Test")
}
 */

try {
    println("test")
    val diffs = StonecutterDecompiler.diffFiles(listOf(
        """
        abc
        abc
        abc
        abc
        123
        123
        123
        4
        4
        4
        4
        
        
        abc
        abc
    """.trimIndent(),
        """
        123
        abc
    """.trimIndent(),
        """
        4
        
        5
    """.trimIndent(),
        """
        extra text abc
        abc
    """.trimIndent()
    ))
    diffs.forEach { println(it) }
    repeat(3) { i ->
        println("$i: ")
        println(diffs.filter { it.files.contains(i) }.joinToString(separator = "\n") { it.content })
    }
} catch (e: Exception) {
    e.printStackTrace()
}