package cloud.util

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

    fun getRelativePath(file: File,rootFile:File):List<String>{
        //相对路径
        val path = mutableListOf<String>()

        var parent = file
        while (parent != rootFile) {
            path.add(0,parent.name)
            parent = parent.parentFile
        }

        return path
    }
}