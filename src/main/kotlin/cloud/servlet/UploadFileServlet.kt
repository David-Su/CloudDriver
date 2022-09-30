package cloud.servlet

import cloud.bean.UploadTask
import cloud.config.Cons
import cloud.manager.UploadTaskManager
import cloud.util.FileUtil
import cloud.manager.logger
import cloud.util.CloudFileUtil
import cloud.util.FFmpegUtil
import cloud.util.TokenUtil
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileUploadException
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.servlet.ServletRequestContext
import org.apache.commons.io.FileCleaningTracker
import java.io.File
import java.io.IOException
import java.math.RoundingMode
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.time.ExperimentalTime

@WebServlet("/uploadfile")
class UploadFileServlet : HttpServlet() {
    // TODO 暂时不支持断点上传
    @Throws(IOException::class, FileUploadException::class)
    override fun doPost(request: HttpServletRequest, resp: HttpServletResponse) {

        var currentFileItem: FileItem? = null
        var currentUploadTask: UploadTask? = null
        var lastCalcSpeedTime: Long? = null //上次用来计算上传速度的时间
        var lastRead = 0L //读取的文件总大小

        val username = TokenUtil.getUsername(request.getParameter("token"))
        val path = CloudFileUtil.getWholePath(request.getParameter("path"))
        val realDir = FileUtil.getWholePath(Cons.Path.DATA_DIR, username, path)
        val tempDir = FileUtil.getWholePath(Cons.Path.TEMP_UPLOAD_DIR, username, path)

        val factory = object : DiskFileItemFactory() {
            override fun createItem(fieldName: String?, contentType: String?, isFormField: Boolean, fileName: String?): FileItem {
                return super.createItem(fieldName, contentType, isFormField, fileName).also {
                    currentFileItem = it
                    currentUploadTask = UploadTask(FileUtil.getWholePath(path, it.name), 0.0, 0).also {
                        UploadTaskManager.updateTask(username, it)
                    }
                    lastCalcSpeedTime = null
                }
            }
        }

        logger.info("realDir->$realDir")
        logger.info("tempDir->$tempDir")

        factory.repository = File(tempDir)

        val upload = ServletFileUpload(factory)

        upload.setProgressListener { read, ContentLength, i ->

            currentFileItem ?: return@setProgressListener
            val localLastCalcSpeedTime = lastCalcSpeedTime
            val speed: Long
            val progress: Double

            if (read == ContentLength) {
                speed = 0
                progress = 1.0
            } else if (localLastCalcSpeedTime == null) {
                speed = 0
                progress = 0.0
            } else {

                speed = System.currentTimeMillis()
                        .takeIf { it > localLastCalcSpeedTime && it - localLastCalcSpeedTime > 500L }
                        ?.let { (read - lastRead) / (it - localLastCalcSpeedTime) * 1000 }
                        ?: return@setProgressListener

                progress = read
                        .toBigDecimal()
                        .divide(ContentLength.toBigDecimal(), 2, RoundingMode.DOWN)
                        .toDouble()
            }

            lastRead = read
            lastCalcSpeedTime = System.currentTimeMillis()

            currentUploadTask?.also {
                it.speed = speed
                it.progress = progress
            }

            UploadTaskManager.updateTask(username)

//            logger.info("progressListener: currentFileItem->${currentFileItem?.name} read->${read} ContentLength->${ContentLength} i->${i}")
        }
        val fileItems: List<FileItem> = upload.parseRequest(ServletRequestContext(request))

        fileItems.forEach { item ->

            val realFile = File(FileUtil.getWholePath(realDir, item.name))
            if (!realFile.exists()) {
                if (!realFile.parentFile.exists()) {
                    realFile.parentFile.mkdirs()
                }
            } else {
                realFile.delete()
            }

            item.write(realFile)

            logger.info("UploadFileServlet item.name->${item.name}")
            logger.info("UploadFileServlet servletContext->${servletContext}")

            val isVideo = servletContext
                    .getMimeType(item.name)
                    ?.startsWith("video")
                    ?: false

            if (isVideo) {
                //生成预览图
                val imagePath = FileUtil.getWholePath(Cons.Path.TEMP_PREVIEW_DIR, username, path, item.name.substringBeforeLast(".") + ".png")

                logger.info("imagePath：${imagePath}")

                FFmpegUtil.extraMiddleFrameImg(realFile.absolutePath, imagePath)
            }

        }


        UploadTaskManager.removeTask(username)

    }
}