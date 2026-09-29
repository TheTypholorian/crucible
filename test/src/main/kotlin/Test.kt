import net.typho.data_util.impl.JsonFormat

object Test {
    @JvmStatic
    fun main(args: Array<String>) {
        var v = 10.times(20)
        println("yay! it works! $v")
        println(JsonFormat().read("{\"abc\": 123}"))
    }
}