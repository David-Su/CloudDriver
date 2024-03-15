package cloud.servlet

import cloud.model.net.CloudFile
import cloud.model.net.CodeMessage
import cloud.model.net.Response
import cloud.config.Cons
import cloud.util.FileUtil
import cloud.manager.logger
import cloud.util.JsonUtil
import cloud.util.TokenUtil
import com.google.common.net.MediaType
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.io.Writer
import java.nio.charset.Charset
import java.util.*
import javax.activation.MimeType
import javax.mail.internet.ContentType
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.collections.ArrayList

@WebServlet("/listfile")
class ListFileServlet : HttpServlet() {
    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val username = TokenUtil.getUsername(req.getParameter("token"))
        val userDir = File(FileUtil.getWholePath(Cons.Path.DATA_DIR, username))
        if (!userDir.exists()) {
            userDir.mkdirs()
        }

        logger.info {
            "userDir->$userDir"
        }

//		FileUtil.deleteFile(new File(path));
        val cloudFile = CloudFile(
                Cons.Path.USER_DIR_STUB,
                true,
                generateCloudFile(userDir, userDir.parentFile),
                getFileLength(userDir),
                null
        )

//        logger.info {
//            "cloudFile->$cloudFile"
//        }

        val writer: Writer = resp.writer

        writer.write(
                JsonUtil.toJson(Response(CodeMessage.OK.code, CodeMessage.OK.message, cloudFile)))
    }

    private fun generateCloudFile(file: File, rootFile: File): List<CloudFile>? {
        val children = file.listFiles()
        if (children.isNullOrEmpty()) return null
        val cloudFiles: MutableList<CloudFile> = ArrayList()
        for (i in children.indices) {
            val child = children[i]
            val isDir = child.isDirectory
            val cloudFile = CloudFile(
                    child.name,
                    isDir,
                    if (isDir) generateCloudFile(child, rootFile) else null,
                    getFileLength(child),
                    assemblePreviewImg(child, rootFile)
            )
            cloudFiles.add(cloudFile)
        }
        return cloudFiles
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

            imgUrl = "/downloadfile?fileType=${fileType}&filePaths=${Base64.getUrlEncoder().encodeToString(previewPath.toByteArray())}"
        }
        return imgUrl
    }

    private fun getFileLength(file: File): Long {
        if (file.isFile) return file.length()
        return file.listFiles()?.sumOf { getFileLength(it) } ?: 0
    }
}