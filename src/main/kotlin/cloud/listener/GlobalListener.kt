package cloud.listener

import cloud.config.Cons
import cloud.util.FFmpegUtil
import cloud.util.FileUtil
import cloud.util.JsonUtil
import com.google.common.net.MediaType
import kotlinx.coroutines.*
import java.io.File
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener

@WebListener
class GlobalListener : ServletContextListener {

    private val scope = CoroutineScope(Dispatchers.Default)
    override fun contextInitialized(sce: ServletContextEvent?) {
        sce ?: return
        val userDir = File(Cons.Path.DATA_DIR)
        if (!userDir.exists()) {
            userDir.mkdirs()
        }
        userDir.walkTopDown().forEach { file ->
            scope.launch {

                val mediaType = sce.servletContext
                        .getMimeType(file.name)
                        .also { it ?: return@launch }
                        .let { MediaType.parse(it) }


                if (!mediaType.`is`(MediaType.ANY_VIDEO_TYPE)) return@launch

                //相对路径
                val path = FileUtil
                        .getRelativePath(file, userDir)
                        //去掉最后一个元素，只要父路径
                        .let { if (it.isNotEmpty()) it.subList(0, it.size - 1) else it }
                val previewParentPath = FileUtil.getWholePath(Cons.Path.TEMP_PREVIEW_DIR, FileUtil.getWholePath(path))
                val preview = File(previewParentPath)
                        .listFiles()
                        ?.find { it.name.substringBeforeLast(".") == file.name.substringBeforeLast(".") }

                if (preview == null) {

                    //生成预览图
                    val imagePath = FileUtil.getWholePath(previewParentPath, file.name.substringBeforeLast(".") + ".png")
//                logger.info("imagePath：${imagePath}")
                    FFmpegUtil.extraMiddleFrameImg(file.absolutePath, imagePath)

                }
            }
        }
    }

    override fun contextDestroyed(sce: ServletContextEvent?) {
        scope.cancel()
    }
}