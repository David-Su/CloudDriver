package cloud.servlet

import cloud.model.net.UploadTask
import cloud.config.Cons
import cloud.manager.UploadTaskManager
import cloud.manager.logger
import cloud.util.*
import jakarta.servlet.http.HttpServlet
import java.io.File
import java.io.IOException
import java.math.RoundingMode
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.fileupload2.core.DiskFileItem
import org.apache.commons.fileupload2.core.DiskFileItemFactory
import org.apache.commons.fileupload2.core.FileItem
import org.apache.commons.fileupload2.core.FileUploadException
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload
import org.apache.commons.io.FileUtils
import java.io.FileOutputStream
import java.nio.file.Files

@WebServlet("/uploadfile")
class UploadFileServlet : HttpServlet() {

    // TODO 暂时不支持断点上传
    @Throws(IOException::class, FileUploadException::class)
    override fun doPost(request: HttpServletRequest, resp: HttpServletResponse) {
        val username = TokenUtil.getUsername(request.getParameter("token"))
        val path = CloudFileUtil.getWholePath(request.getParameter("path"), username)
        //客户端显示的路径
        val clientPath = CloudFileUtil.getWholePath(request.getParameter("path"), Cons.Path.USER_DIR_STUB)
        val realDir = FileUtil.getWholePath(Cons.Path.DATA_DIR, path)
        val tempDir = FileUtil.getWholePath(Cons.Path.TEMP_UPLOAD_DIR, path)

        val factory = DiskFileItemFactory.builder()
            .setFile(File(tempDir))
            .get()

//        val factory = object : DiskFileItemFactory() {
//            override fun createItem(fieldName: String?, contentType: String?, isFormField: Boolean, fileName: String?): FileItem {
//                return super.createItem(fieldName, contentType, isFormField, fileName).also {
//                    currentFileItem = it
//                    currentUploadTask = UploadTask(FileUtil.getWholePath(clientPath, it.name), 0.0, 0).also {
//                        UploadTaskManager.addTask(username, it)
//                    }
//                    lastCalcSpeedTime = null
//                }
//            }
//        }

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

        val upload = JakartaServletFileUpload(factory)

        //只拿第一个
        val fileItemInput = upload.getItemIterator(request)
            .takeIf { it.hasNext() }
            ?.next()
            ?: return

        val fileItem = upload.fileItemFactory.fileItemBuilder()
            .setFieldName(fileItemInput.fieldName)
            .setContentType(fileItemInput.contentType)
            .setFormField(fileItemInput.isFormField)
            .setFileName(fileItemInput.name)
            .setFileItemHeaders(fileItemInput.headers)
            .get()
        val input = fileItemInput.inputStream
        val output = fileItem.outputStream
        val contentLength = request.contentLengthLong
        val bufferSize = DEFAULT_BUFFER_SIZE
        val fileName = fileItemInput.name
        val isVideo = servletContext
            .getMimeType(fileName)
            ?.startsWith("video")
            ?: false
        //更新进度的最小间隔
        val updateTaskSpan = 500L

        val realFile = File(FileUtil.getWholePath(realDir, fileName))
        val currentUploadTask = UploadTask(FileUtil.getWholePath(clientPath, fileName), 0.0, 0)

        var lastCalcSpeedTime: Long? = null //上次用来计算上传速度的时间

        UploadTaskManager.addTask(username, currentUploadTask)

        try {//将上传的数据写入文件缓存
            fileItem.delete()
            input.use {
                output.use {
                    var lastRead = 0L //读取的文件总大小
                    var bytesCopied: Long = 0
                    val buffer = ByteArray(bufferSize)

                    while (true) {
                        val bytes = input.read(buffer)

                        if (bytes < 0) break

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
                                .takeIf { it > localLastCalcSpeedTime && it - localLastCalcSpeedTime > updateTaskSpan }
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

                Files.move(fileItem.path, realFile.toPath())

                if (isVideo) { //生成预览图
                    val imagePath = FileUtil.getWholePath(
                        Cons.Path.TEMP_PREVIEW_DIR,
                        path,
                        "${fileName.substringBeforeLast(".")}_temp" + ".png"
                    )
                    FFmpegUtil.extraMiddleFrameImg(realFile.absolutePath, imagePath)
                    val compressImagePath = FileUtil.getWholePath(Cons.Path.TEMP_PREVIEW_DIR, path, fileName.substringBeforeLast(".") + ".jpg")
                    ImageCompressUtil.previewCompress(imagePath,compressImagePath)
                    logger.info { "压缩图片: 原大小->${File(imagePath).length()}  压缩后大小->${File(compressImagePath).length()}" }
                    FileUtil.deleteFile(File(imagePath))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        UploadTaskManager.removeTask(username, currentUploadTask)
    }
}