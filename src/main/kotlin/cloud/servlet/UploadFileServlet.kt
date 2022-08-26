package cloud.servlet

import cloud.config.Cons
import cloud.util.FileUtil
import cloud.manager.logger
import cloud.util.CloudFileUtil
import cloud.util.FFmpegUtil
import cloud.util.TokenUtil
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileUploadException
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.servlet.ServletRequestContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@WebServlet("/uploadfile")
class UploadFileServlet : HttpServlet() {
    // TODO 暂时不支持断点上传
    @OptIn(ExperimentalTime::class)
    @Throws(IOException::class, FileUploadException::class)
    override fun doPost(request: HttpServletRequest, resp: HttpServletResponse) {
        val username = TokenUtil.getUsername(request.getParameter("token"))
        val path = CloudFileUtil.getWholePath(request.getParameter("path"))
        val realDir = FileUtil.getWholePath(Cons.Path.DATA_DIR, username, path)
        val tempDir = FileUtil.getWholePath(Cons.Path.TEMP_UPLOAD_DIR, username, path)
        logger.info("realDir->$realDir")
        logger.info("tempDir->$tempDir")
        val upload = ServletFileUpload(DiskFileItemFactory().also {
            it.repository = File(tempDir)
        })

        val list: List<FileItem>

        logger.info("parseRequest耗时：${measureTime { list = upload.parseRequest(ServletRequestContext(request)) }.inWholeSeconds}")

        list.forEach { item ->

            val realFile = File(FileUtil.getWholePath(realDir, item.name))
            if (!realFile.exists()) {
                if (!realFile.parentFile.exists()) {
                    realFile.parentFile.mkdirs()
                }
            } else {
                realFile.delete()
            }

            logger.info("写入主文件耗时：${measureTime { item.write(realFile) }.inWholeSeconds}")


            //生成预览图
            val imagePath = FileUtil.getWholePath(Cons.Path.TEMP_PREVIEW_DIR, username, path, item.name.substringBeforeLast(".") + ".png")

            logger.info("imagePath：${imagePath}")

            FFmpegUtil.extraMiddleFrameImg(realFile.absolutePath, imagePath)

        }


    }
}