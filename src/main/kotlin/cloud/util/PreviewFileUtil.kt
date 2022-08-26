package cloud.util

import cloud.config.Cons
import cloud.manager.logger
import java.io.File
import java.util.*

object PreviewFileUtil {

    fun getPreviewFile(file: File, rootFile: File): File? {
        var preview: File? = null

        if (file.isFile) {

            //相对路径
            val path = FileUtil
                    .getRelativePath(file, rootFile)
                    //去掉最后一个元素，只要父路径
                    .let { if (it.isNotEmpty()) it.subList(0, it.size - 1) else it }

            val previewParentPath = FileUtil.getWholePath(Cons.Path.TEMP_PREVIEW_DIR, FileUtil.getWholePath(path))

            logger.info("previewParentPath -> ${previewParentPath}")

            preview = File(previewParentPath)
                    .listFiles()
                    ?.find { it.name.substringBeforeLast(".") == file.name.substringBeforeLast(".") }
                    ?: return null
        }
        return preview
    }

}