package cloud.config

import java.io.File

object FileUtil {
    fun getWholePath(paths: List<String>): String {
        return getWholePath(*paths.toTypedArray())
    }

    fun getWholePath(vararg paths: String): String {
        val wholePath = StringBuilder("")
        for (i in paths.indices) {
            if (i > 0) {
                wholePath.append(File.separator)
            }
            wholePath.append(paths[i])
        }
        return wholePath.toString()
    }

    fun deleteFile(file: File) {
        if (file.isFile) {
            file.delete()
            return
        }
        val files = file.listFiles()
        for (i in files.indices) {
            deleteFile(files[i])
        }
        file.delete()
    }
}