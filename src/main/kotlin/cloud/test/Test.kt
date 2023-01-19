package cloud.test

import java.io.IOException
import java.net.URLDecoder




object Test {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
//        Baidu().main()
//        Zip().main()
        Video.getFrame()

        val decode = URLDecoder.decode("XF-test0116%250B%2500ltlovezh", "UTF-8")

    }
}