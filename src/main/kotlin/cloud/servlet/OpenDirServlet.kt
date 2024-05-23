package cloud.servlet

import cloud.config.Cons
import cloud.manager.logger
import cloud.model.net.*
import cloud.util.CloudFileUtil
import cloud.util.FileUtil
import cloud.util.JsonUtil
import cloud.util.TokenUtil
import com.google.common.net.MediaType
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.File
import java.io.Writer
import java.util.*

@WebServlet("/opendir")
class OpenDirServlet : HttpServlet() {

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val path = TokenUtil.getUsername(req.getParameter("token"))
            .let { CloudFileUtil.getWholePath(JsonUtil.fromJsonReader(req.reader, OpenDir::class.java).paths, it) }
            .let { FileUtil.getWholePath(Cons.Path.DATA_DIR, it) }

        val targetDirFile = File(path)

        val writer: Writer = resp.writer

        if (!targetDirFile.exists() || !targetDirFile.isDirectory) {
            Response<Unit>(CodeMessage.DIR_OR_FILE_NOT_EXIST.code, CodeMessage.DIR_OR_FILE_NOT_EXIST.message)
                .let { JsonUtil.toJson(it) }
                .also { writer.write(it) }
            return
        }

        val dataDir = File(Cons.Path.DATA_DIR)

        val children = targetDirFile.listFiles()
            ?.map {
                DirCloudFile.DirCloudFileChild(
                    it.name, it.isDirectory, assemblePreviewImg(it, dataDir)
                )
            }

        val dirCloudFile = DirCloudFile(targetDirFile.name, children)

        Response(CodeMessage.OK.code, CodeMessage.OK.message, dirCloudFile)
            .let { JsonUtil.toJson(it) }
            .also { writer.write(it) }
    }

    private fun assemblePreviewImg(file: File, rootFile: File): String? {
        var imgUrl: String? = null

        if (file.isFile) {

            //相对路径
            val path = FileUtil
                .getRelativePath(file, rootFile)
                //去掉最后一个元素，只要父路径
                .let { if (it.isNotEmpty()) it.subList(0, it.size - 1) else it }


            val previewParentPath = FileUtil.getWholePath(Cons.Path.TEMP_PREVIEW_DIR, FileUtil.getWholePath(path))

            logger.info {
                "previewParentPath:${previewParentPath}"
            }

            val preview = File(previewParentPath)
                .listFiles()
                ?.find { it.name.substringBeforeLast(".") == file.name.substringBeforeLast(".") }

            val fileType: Int
            val previewPath: String

            if (preview != null) { //能找到预览图就用预览图
                fileType = DownloadFileServlet.FILE_TYPE_TEMP_PREVIEW
                previewPath = path.toMutableList()
                    .also { it.add(preview.name) }
                    //username文件夹用占位符替代，DownloadFileServlet会用username取代
                    .also { it[0] = Cons.Path.USER_DIR_STUB }
                    .let { JsonUtil.toJson(it) }
            } else {
                val mimeType = servletContext.getMimeType(file.name) ?: return null
                val mediaType = MediaType.parse(mimeType)
                when {
                    mediaType.`is`(MediaType.ANY_IMAGE_TYPE) -> { //如果是图片类型的,直接返回一个文件下载链接
                        fileType = DownloadFileServlet.FILE_TYPE_DATA
                        previewPath = path.toMutableList()
                            .also { it.add(file.name) }
                            //username文件夹用占位符替代，DownloadFileServlet会用username取代
                            .also { it[0] = Cons.Path.USER_DIR_STUB }
                            .let { JsonUtil.toJson(it) }
                    }

                    else -> {
                        return null
                    }
                }
            }

            logger.info {
                "previewPath:${previewPath}"
            }

            imgUrl = "/downloadfile?fileType=${fileType}&filePaths=${
                Base64.getUrlEncoder().encodeToString(previewPath.toByteArray())
            }"
        }
        return imgUrl
    }
}