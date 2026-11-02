import net.typho.data_util.SerializedData
import net.typho.data_util.json.JsonFormat

object Test {
    @JvmStatic
    fun main(args: Array<String>) {
        val v = 10.times(20)
        println("yay! it works! $v")
        println(JsonFormat.read(SerializedData("{\"abc\": 123}")))
        SideClass.side()
        JavaClass.sayHi()
    }
}