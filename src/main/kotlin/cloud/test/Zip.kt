package cloud.test

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.progress.ProgressMonitor
import java.io.File
import java.util.*

class Zip {
    fun main() {
        val params = ZipParameters()

//		params.setCompressionLevel(CompressionLevel.ULTRA);
//		List<File> filesToAdd = Arrays.asList(new File("C:\\Users\\admin\\Desktop\\CloudDriver"));
        val now = Date()
        val file = File("C:\\Users\\admin\\Desktop\\zip\\CloudDriver.zip")
        val zipFile = ZipFile(file)
        val progressMonitor = zipFile.progressMonitor
        zipFile.isRunInThread = true

//		try {
//			zipFile.addFolder(new File("C:\\Users\\admin\\Desktop\\CloudDriver"));
//		} catch (ZipException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        try {
            zipFile.createSplitZipFileFromFolder(File("C:\\Users\\admin\\Desktop\\CloudDriver"), params, true,
                    1024 * 1024 * 1024 * 2L)
            while (progressMonitor.state != ProgressMonitor.State.READY) {
                println("Percentage done: " + progressMonitor.percentDone)
                println("Current file: " + progressMonitor.fileName)
                println("Current task: " + progressMonitor.currentTask)
                Thread.sleep(100)
            }

//			zipFile.extractAll("C:\\\\Users\\\\admin\\\\Desktop\\\\result");
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val interval = Date().time - now.time
        print("${interval / 1000}")
    }
}