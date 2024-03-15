package cloud.test

import it.sauronsoftware.jave.Encoder
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.Java2DFrameConverter
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import javax.imageio.ImageIO
import kotlin.concurrent.timerTask


object Video {

    fun getFrame() {
//        val file = File("C:\\Users\\admin\\Desktop\\2.avi")
//        val ff = FFmpegFrameGrabber(file)
//        ff.start()
//        val frameLength = ff.lengthInVideoFrames
//
//        var frame: Frame? = null
//
//        for (i in 0 until frameLength) {
//            if (i > frameLength / 3 && ff.grabFrame()?.also { frame = it }?.image != null) break
//        }
//
//        if (frame == null) return;
//
//        val bufferImage = Java2DFrameConverter().getBufferedImage(frame)
//
//        ImageIO.write(bufferImage, "jpg", File("C:\\Users\\admin\\Desktop\\boys.jpg"))
//
//        ff.flush()
//        ff.stop()


//        frameExtract("C:\\Users\\admin\\Desktop\\黑客帝国\\Matrix.mkv", "C:\\Users\\admin\\Desktop\\boy\\boys.jpg")
        val start = System.currentTimeMillis()
        frameExtract2("C:\\Users\\admin\\Desktop\\黑客帝国\\Matrix.mkv", "C:\\Users\\admin\\Desktop\\boy\\boys.jpg")
        frameExtract2("C:\\Users\\admin\\Desktop\\黑客帝国\\Matrix2.mkv", "C:\\Users\\admin\\Desktop\\boy\\boys.jpg")
        print("time consuming:${System.currentTimeMillis() - start}")



        Timer().schedule(timerTask {
            measureTime("第二次") {
                frameExtract2("C:\\Users\\admin\\Desktop\\黑客帝国\\Matrix.mkv", "C:\\Users\\admin\\Desktop\\boy\\boys.jpg")
                frameExtract2("C:\\Users\\admin\\Desktop\\黑客帝国\\Matrix2.mkv", "C:\\Users\\admin\\Desktop\\boy\\boys.jpg")
            }
        },5000)
    }

    private fun frameExtract2(videoPath: String, imgPath: String) {
        val ff: FFmpegFrameGrabber = measureTime("FFmpegFrameGrabber.createDefault"){FFmpegFrameGrabber.createDefault(videoPath)}


        measureTime("ff.start()"){ff.start()}


        val ffLength = ff.lengthInFrames
        var f: Frame
        var i = 0

        ff.setVideoFrameNumber(ffLength / 2)

        measureTime("doExecuteFrame"){doExecuteFrame(measureTime("grabImage") { ff.grabImage() }, imgPath)}


//        while (i < ffLength) {
//            f = ff.grabImage()
//            //截取第6帧
//            if (i > 600 && f.image != null) {
//                //执行截图并放入指定位置
//                doExecuteFrame(f, imgPath)
//                break
//            }
//            i++
//        }
        ff.stop()
    }

    private fun doExecuteFrame(f: Frame?, targerFilePath: String) {
        val imagemat = "png"
        if (null == f || null == f.image) {
            return
        }
        val converter = measureTime("Java2DFrameConverter"){Java2DFrameConverter()}
        val bi = measureTime("getBufferedImage"){converter.getBufferedImage(f)}
        val output = File(targerFilePath)
        try {
            measureTime("ImageIO.write"){ImageIO.write(bi, imagemat, output)}
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun frameExtract(videoPath: String, imgPath: String) {

        val start = System.currentTimeMillis()

        val midSeconds = Encoder()
                .getInfo(File(videoPath))
                .duration
                .let { it / 1000 }
                .let { it / 2 }

        print("midSecond:${midSeconds}")

        val hours = midSeconds / 3600

        val rem = midSeconds % 3600

        val minutes = rem / 60

        val seconds = rem % 60


        val formatTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        print("formatTime:${formatTime}")


        val imgPathFolder = File(imgPath);
        if (!imgPathFolder.exists()) {
            imgPathFolder.mkdirs();
        }
        val path = "D:\\ffmpeg\\bin\\ffmpeg.exe";
        val processBuilder = ProcessBuilder(path,
                "-i", videoPath,
//                "-ss", "00:00:00",
                "-ss", formatTime,
                "-frames:v", "1",
//                "-qscale:v", "2",
//                "-f", "image2",
//                "-vsync", "2",
                imgPath)
        processBuilder.redirectErrorStream(true);
        val process = processBuilder.start()
        val processOutput = StringBuilder();
        try {
            val processOutputReader = BufferedReader(
                    InputStreamReader(process.inputStream))

            var readLine: String?
            while (processOutputReader.readLine().also { readLine = it } != null) {
                processOutput.append(readLine + System.lineSeparator());
            }
            process.waitFor();
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace();
        } finally {
            process?.destroy()
        }

        print("time consuming:${System.currentTimeMillis() - start}")
    }


    private fun <T> measureTime(tag: String, function: () -> T): T {
        val start = System.currentTimeMillis()
        val result = function.invoke()
        println("$tag time consume -> ${System.currentTimeMillis() - start}")
        return result
    }
}