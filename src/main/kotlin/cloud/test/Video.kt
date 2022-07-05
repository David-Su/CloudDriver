package cloud.test

import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.Java2DFrameConverter
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.imageio.ImageIO

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


        frameExtract("C:\\Users\\admin\\Desktop\\2.avi","C:\\Users\\admin\\Desktop\\boys.jpg")
    }


    fun frameExtract(videoPath: String, imgPath: String) {

        val imgPathFolder = File(imgPath);
        if (!imgPathFolder.exists()) {
            imgPathFolder.mkdirs();
        }
        val path = "D:\\ffmpeg\\bin\\ffmpeg.exe";
        val processBuilder = ProcessBuilder(path, "-y",
                "-i", videoPath,
                "-ss", "00:00:00",
                "-qscale:v", "2",
                "-f", "image2",
                "-vsync", "2",
                imgPath + "%d.jpg");
        processBuilder.redirectErrorStream(true);
        val process = processBuilder.start()
        val processOutput = StringBuilder();
        try {
            val processOutputReader = BufferedReader(
                    InputStreamReader(process.getInputStream()))

            var readLine: String
            while (processOutputReader.readLine().also { readLine = it } != null) {
                processOutput.append(readLine + System.lineSeparator());
            }
            process.waitFor();
        } catch (ex:java.lang.Exception) {
            ex.printStackTrace();
        } finally {
            process?.destroy()
        }
    }

}