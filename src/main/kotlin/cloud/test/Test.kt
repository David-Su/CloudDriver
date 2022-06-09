package cloud.test

import java.io.IOException

object Test {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        Baidu().main()
        Zip().main()
    }
}