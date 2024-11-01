package cloud.listener

import cloud.config.Cons
import cloud.manager.logger
import cloud.util.FFmpegUtil
import cloud.util.FileUtil
import cloud.util.ImageCompressUtil
import com.google.common.net.MediaType
import kotlinx.coroutines.*
import java.io.File
import jakarta.servlet.ServletContextEvent
import jakarta.servlet.ServletContextListener
import jakarta.servlet.annotation.WebListener
import kotlinx.coroutines.flow.*
import org.apache.commons.io.FileUtils

@WebListener
class GlobalListener : ServletContextListener {

    private val context = SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        logger.info {
            "contextInitialized异常:${throwable.printStackTrace()}"
        }
    }

    override fun contextInitialized(sce: ServletContextEvent?): Unit = runBlocking(context = context) {

        val startTime = System.currentTimeMillis()

        sce ?: return@runBlocking

        val userDir = File(Cons.Path.DATA_DIR)
        if (!userDir.exists()) {
            userDir.mkdirs()
            return@runBlocking
        }

//        syncImpl(userDir, sce, startTime)

        userDir.walkTopDown()
            .asFlow()
            .filter { it.isFile }
            .filter {
                sce.servletContext
                    .getMimeType(it.name)
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { MediaType.parse(it) }
                    ?.let { it.`is`(MediaType.ANY_VIDEO_TYPE) or it.`is`(MediaType.ANY_IMAGE_TYPE) }
                    ?: false
            }
            .flatMapMerge { file ->
                flow<Unit> {
                    val taskStartTime = System.currentTimeMillis()

                    val mimeType = sce.servletContext
                        .getMimeType(file.name)
                        .let { MediaType.parse(it) }

                    //相对路径
                    val path = FileUtil
                        .getRelativePath(file, userDir)
                        //去掉最后一个元素，只要父路径
                        .let { if (it.isNotEmpty()) it.subList(0, it.size - 1) else it }
                    val previewParentPath =
                        FileUtil.getWholePath(Cons.Path.TEMP_PREVIEW_DIR, FileUtil.getWholePath(path))
                    val preview = File(previewParentPath)
                        .listFiles()
                        ?.find { it.name.substringBeforeLast(".") == file.name }

                    logger.info { "压缩图片-找到对应的预览图:${file.name}  ${preview}" }

                    if (preview != null) return@flow

                    //生成压缩前预览图
                    val tempImagePath = FileUtil.getWholePath(
                        previewParentPath,
                        "${file.name}_temp" + ".png"
                    )

                    val tempImageFile = File(tempImagePath)

                    if (tempImageFile.exists() && tempImageFile.isFile) {
                        tempImageFile.delete()
                    } else if (!tempImageFile.parentFile.exists()) {
                        tempImageFile.parentFile.mkdirs()
                    }

                    when {
                        mimeType.`is`(MediaType.ANY_VIDEO_TYPE) -> {
                            //使用IO调度器获取视频某一帧的图片
                            withContext(Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
                                throwable.printStackTrace()
                                logger.info { "压缩图片-extraMiddleFrameImg异常:${file.name}  ${throwable}" }
                            }) {
                                logger.info { "压缩图片-获取视频某一帧的图片-进入协程:${file.name}" }
                                FFmpegUtil.extraMiddleFrameImg(file.absolutePath, tempImagePath)
                            }
                        }

                        mimeType.`is`(MediaType.ANY_IMAGE_TYPE) -> {
                            file.copyTo(tempImageFile)
                        }

                        else -> {
                            return@flow
                        }
                    }


                    val compressImagePath = FileUtil.getWholePath(
                        previewParentPath,
                        "${file.name}.jpg"
                    )
                    //使用Default调度器压缩图片
                    withContext(Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
                        throwable.printStackTrace()
                        logger.info { "压缩图片-压缩图片异常:${file.name}  ${throwable}" }
                    }) {
                        logger.info { "压缩图片-压缩图片-进入协程:${file.name}  " }
                        ImageCompressUtil.previewCompress(tempImagePath, compressImagePath)
                    }
                    logger.info {
                        val imageFile = File(tempImagePath)
                        val compressedFile = File(compressImagePath)
                        buildString {
                            append("压缩图片-结束:${file.name}  ")
                            appendLine()
                            append("图片路径: ${imageFile.absolutePath}->${compressedFile.absolutePath}")
                            appendLine()
                            append("图片大小: ${imageFile.length()}->${compressedFile.length()}")
                            appendLine()
                            append("耗时: ${System.currentTimeMillis() - taskStartTime}")
                        }
                    }
                    FileUtil.deleteFile(File(tempImagePath))
                }
            }
            .collect()

        logger.info {
            "压缩图片总耗时:${System.currentTimeMillis() - startTime}"
        }
    }

    private fun syncImpl(userDir: File, sce: ServletContextEvent, startTime: Long) {
        val deferreds = userDir.walkTopDown()
            .filter { it.isFile }
            .filter {
                sce.servletContext
                    .getMimeType(it.name)
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { MediaType.parse(it) }
                    ?.`is`(MediaType.ANY_VIDEO_TYPE)
                    ?: false
            }
            .forEach { file ->

                logger.info { "压缩图片-检查源文件:${file.absolutePath}" }
                val startTime = System.currentTimeMillis()

                //相对路径
                val path = FileUtil
                    .getRelativePath(file, userDir)
                    //去掉最后一个元素，只要父路径
                    .let { if (it.isNotEmpty()) it.subList(0, it.size - 1) else it }
                val previewParentPath =
                    FileUtil.getWholePath(Cons.Path.TEMP_PREVIEW_DIR, FileUtil.getWholePath(path))
                val preview = File(previewParentPath)
                    .listFiles()
                    ?.find { it.name.substringBeforeLast(".") == file.name.substringBeforeLast(".") }

                logger.info { "压缩图片-找到对应的预览图:${file.name}  ${preview}" }

                if (preview == null) {
                    //生成预览图
                    val imagePath = FileUtil.getWholePath(
                        previewParentPath,
                        "${file.name.substringBeforeLast(".")}_temp" + ".png"
                    )
                    logger.info { "压缩图片-获取视频某一帧的图片" }
                    //使用IO调度器获取视频某一帧的图片
                    FFmpegUtil.extraMiddleFrameImg(file.absolutePath, imagePath)
                    val compressImagePath = FileUtil.getWholePath(
                        previewParentPath,
                        file.name.substringBeforeLast(".") + ".jpg"
                    )
                    logger.info { "压缩图片-压缩图片:${file.name}  " }
                    //使用Default调度器压缩图片
                    ImageCompressUtil.previewCompress(imagePath, compressImagePath)
                    logger.info {
                        val imageFile = File(imagePath)
                        val compressedFile = File(compressImagePath)
                        buildString {
                            append("压缩图片-结束:${file.name}  ")
                            appendLine()
                            append("图片路径: ${imageFile.absolutePath}->${compressedFile.absolutePath}")
                            appendLine()
                            append("图片大小: ${imageFile.length()}->${compressedFile.length()}")
                            appendLine()
                            append("耗时: ${System.currentTimeMillis() - startTime}")
                            appendLine()
                        }
                    }
                    FileUtils.delete(File(imagePath))
                }
            }

        logger.info { "全局初始化耗时:${System.currentTimeMillis() - startTime}" }
    }

    override fun contextDestroyed(sce: ServletContextEvent) {
        logger.info { "contextDestroyed:$sce" }
        context.cancel()
    }
}