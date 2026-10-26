repositories {
    mavenCentral()
    typhoNet()
}

println("test")

val module = modules.register("main") {
    println("test")

    dependencies {
        kotlin("stdlib")
        add("net.typho:data_util:1.4.2")
        add("net.typho:asm_util:1.3.6")
    }

    println("test")

    java()
    println("test")
    resources()
    println("test")
    kotlin()
    println("test")

    compileTask()
    jarTask()
    runTask(mainClass = "Test")
}

println("test2")
