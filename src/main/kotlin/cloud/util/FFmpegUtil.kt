package cloud.util

import cloud.manager.logger
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

object FFmpegUtil {

    fun extraMiddleFrameImg(videoPath: String, outPutPath: String): Boolean {
        val grabber: FFmpegFrameGrabber = try {
            FFmpegFrameGrabber.createDefault(videoPath)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        try {
            grabber.start()
            //设置当前帧数为中间位置
            grabber.setVideoFrameNumber(grabber.lengthInFrames / 2)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        val outPutFile = File(outPutPath)

        if (!outPutFile.parentFile.exists()) outPutFile.parentFile.mkdirs()

        outPutFile.delete()

        val converter = Java2DFrameConverter()
        val bi = converter.getBufferedImage(grabber.grabImage()) ?: return false

        try {
            ImageIO.write(bi, outPutFile.extension, outPutFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        logger.info { "关闭grabber" }
        grabber.close()

        return outPutFile.exists()
    }

}