package cloud.test

import java.io.IOException
import java.net.URLDecoder
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.Path


object Test {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
//        Baidu().main()
//        Zip().main()

        println(System.getProperty("user.home"))

//        Files.walkFileTree(Path("C:\\Users\\david\\Documents\\flutter\\bin\\cache\\artifacts\\engine"), object : SimpleFileVisitor<Path>() {
//            override fun preVisitDirectory(dir: Path?, attrs: BasicFileAttributes?): FileVisitResult {
//                println("preVisitDirectory: "+dir.toString())
//                return super.preVisitDirectory(dir, attrs)
//            }
//
//            override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
//                println("visitFile: "+file.toString())
//                return super.visitFile(file, attrs)
//            }
//
//            override fun postVisitDirectory(dir: Path?, exc: IOException?): FileVisitResult {
//                println("postVisitDirectory: "+dir.toString())
//                return super.postVisitDirectory(dir, exc)
//            }
//        })

    }
}