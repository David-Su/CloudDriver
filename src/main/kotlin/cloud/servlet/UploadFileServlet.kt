package cloud.servlet

import cloud.model.net.UploadTask
import cloud.config.Cons
import cloud.manager.UploadTaskManager
import cloud.manager.logger
import cloud.util.*
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileUploadException
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.servlet.ServletRequestContext
import java.io.File
import java.io.IOException
import java.math.RoundingMode
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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
        val path = CloudFileUtil.getWholePath(request.getParameter("path"), username)
        //客户端显示的路径
        val clientPath = CloudFileUtil.getWholePath(request.getParameter("path"), Cons.Path.USER_DIR_STUB)
        val realDir = FileUtil.getWholePath(Cons.Path.DATA_DIR, path)
        val tempDir = FileUtil.getWholePath(Cons.Path.TEMP_UPLOAD_DIR,path)

        val factory = object : DiskFileItemFactory() {
            override fun createItem(fieldName: String?, contentType: String?, isFormField: Boolean, fileName: String?): FileItem {
                return super.createItem(fieldName, contentType, isFormField, fileName).also {
                    currentFileItem = it
                    currentUploadTask = UploadTask(FileUtil.getWholePath(clientPath, it.name), 0.0, 0).also {
                        UploadTaskManager.addTask(username, it)
                    }
                    lastCalcSpeedTime = null
                }
            }
        }

        logger.info {
            buildString {
                append("\n")
                append("path->$path")
                append("\n")
                append("realDir->$realDir")
                append("\n")
                append("tempDir->$tempDir")
            }
        }

        factory.repository = File(tempDir)

        val upload = ServletFileUpload(factory)

        upload.setProgressListener { read, contentLength, i ->

            currentFileItem ?: return@setProgressListener
            val localLastCalcSpeedTime = lastCalcSpeedTime
            val speed: Long
            val progress: Double

            if (read == contentLength) {
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
                        .divide(contentLength.toBigDecimal(), 2, RoundingMode.DOWN)
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

        val fileItems: List<FileItem>? = try {
            upload.parseRequest(ServletRequestContext(request))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        fileItems?.forEach { item ->

            val realFile = File(FileUtil.getWholePath(realDir, item.name))

            val isVideo = servletContext
                    .getMimeType(item.name)
                    ?.startsWith("video")
                    ?: false

            synchronized(realFile.absolutePath.intern()) {
                if (!realFile.exists()) {
                    if (!realFile.parentFile.exists()) {
                        realFile.parentFile.mkdirs()
                    }
                } else {
                    realFile.delete()
                }

                item.write(realFile)

                if (isVideo) {
                    //生成预览图
                    val imagePath = FileUtil.getWholePath(Cons.Path.TEMP_PREVIEW_DIR, path, item.name.substringBeforeLast(".") + ".png")
//                logger.info("imagePath：${imagePath}")
                    FFmpegUtil.extraMiddleFrameImg(realFile.absolutePath, imagePath)
                }
            }

        }

        currentUploadTask?.also { UploadTaskManager.removeTask(username, it) }

    }
}