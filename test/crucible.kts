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
    StonecutterDecompiler.diffFiles(listOf(
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
    )).forEach { println(it) }
} catch (e: Exception) {
    e.printStackTrace()
}