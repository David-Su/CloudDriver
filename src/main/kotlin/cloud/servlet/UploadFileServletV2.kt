package cloud.servlet

import cloud.model.net.UploadTask
import cloud.config.Cons
import cloud.manager.UploadTaskManager
import cloud.manager.logger
import cloud.util.*
import com.google.common.io.Files
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileUploadException
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.servlet.ServletRequestContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.math.RoundingMode
import javax.servlet.annotation.MultipartConfig
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet("/uploadfilev2")
@MultipartConfig
class UploadFileServletV2 : HttpServlet() {
    // TODO 暂时不支持断点上传
    @Throws(IOException::class, FileUploadException::class)
    override fun doPost(request: HttpServletRequest, resp: HttpServletResponse) {

        val username = TokenUtil.getUsername(request.getParameter("token"))
        val path = CloudFileUtil.getWholePath(request.getParameter("path"), username)
        //客户端显示的路径
        val clientPath = CloudFileUtil.getWholePath(request.getParameter("path"), Cons.Path.USER_DIR_STUB)
        val realDir = FileUtil.getWholePath(Cons.Path.DATA_DIR, path)
        val tempDir = FileUtil.getWholePath(Cons.Path.TEMP_UPLOAD_DIR, path)

        logger.info {
            buildString {
                append("\n")
                append("path->$path")
                append("\n")
                append("realDir->$realDir")
                append("\n")
                append("tempDir->$tempDir")
                append("\n")
                append("part->${request.getPart("file")}")
                append("\n")
                append("part->${request.parts.size}")
            }
        }

        request.parts.forEach { part ->
            var lastCalcSpeedTime: Long? = null //上次用来计算上传速度的时间
            val contentLength = part.size
            val bufferSize = DEFAULT_BUFFER_SIZE
            val fileName = part.submittedFileName
            val isVideo = servletContext
                    .getMimeType(fileName)
                    ?.startsWith("video")
                    ?: false
            val saveFile = File(buildString {
                append(tempDir)
                append(File.separator)
                append(fileName)
            })
            val realFile = File(FileUtil.getWholePath(realDir, fileName))
            val currentUploadTask = UploadTask(FileUtil.getWholePath(clientPath, fileName), 0.0, 0).also {
                UploadTaskManager.addTask(username, it)
            }

            //将上传的数据写入文件缓存
            FileUtil.deleteFile(saveFile)
            part.inputStream.use { input ->

                FileOutputStream(saveFile).use { output ->
                    var lastRead = 0L //读取的文件总大小
                    var bytesCopied: Long = 0
                    val buffer = ByteArray(bufferSize)

                    while (true) {
                        val bytes = input.read(buffer)

                        if (bytes <0) break

                        output.write(buffer, 0, bytes)

                        bytesCopied += bytes

                        val localLastCalcSpeedTime = lastCalcSpeedTime
                        val speed: Long
                        val progress: Double
                        if (bytesCopied >= contentLength) {
                            speed = 0
                            progress = 1.0
                        } else if (localLastCalcSpeedTime == null) {
                            speed = 0
                            progress = 0.0
                        } else {

                            speed = System.currentTimeMillis()
                                    .takeIf { it > localLastCalcSpeedTime && it - localLastCalcSpeedTime > 500L }
                                    ?.let { (bytesCopied - lastRead) / (it - localLastCalcSpeedTime) * 1000 }
                                    ?: continue

                            logger.info {
                                "bytesCopied->${bytesCopied} lastRead->${lastRead} speed->${speed}"
                            }

                            progress = bytesCopied
                                    .toBigDecimal()
                                    .divide(contentLength.toBigDecimal(), 2, RoundingMode.DOWN)
                                    .toDouble()
                        }

                        lastRead = bytesCopied
                        lastCalcSpeedTime = System.currentTimeMillis()

                        currentUploadTask.also {
                            it.speed = speed
                            it.progress = progress
                        }

                        UploadTaskManager.updateTask(username)

                    }
                }

                currentUploadTask.also { UploadTaskManager.removeTask(username, it) }

            }

            //将文件缓存移动到用户文件夹
            FileUtil.deleteFile(realFile)
            synchronized(realFile.absolutePath.intern()) {
                if (!realFile.exists()) {
                    if (!realFile.parentFile.exists()) {
                        realFile.parentFile.mkdirs()
                    }
                } else {
                    realFile.delete()
                }

                Files.move(saveFile, realFile)

                if (isVideo) { //生成预览图
                    val imagePath = FileUtil.getWholePath(Cons.Path.TEMP_PREVIEW_DIR, path, fileName.substringBeforeLast(".") + ".png")
//                logger.info("imagePath：${imagePath}")
                    FFmpegUtil.extraMiddleFrameImg(realFile.absolutePath, imagePath)
                }
            }
        }

    }
}