package cloud.test;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.crypto.Data;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.progress.ProgressMonitor;

public class Zip {

	public void main() {
		ZipParameters params = new ZipParameters();

//		params.setCompressionLevel(CompressionLevel.ULTRA);
//		List<File> filesToAdd = Arrays.asList(new File("C:\\Users\\admin\\Desktop\\CloudDriver"));

		Date now = new Date();

		File file = new File("C:\\Users\\admin\\Desktop\\zip\\CloudDriver.zip");

		ZipFile zipFile = new ZipFile(file);

		ProgressMonitor progressMonitor = zipFile.getProgressMonitor();

		zipFile.setRunInThread(true);

//		try {
//			zipFile.addFolder(new File("C:\\Users\\admin\\Desktop\\CloudDriver"));
//		} catch (ZipException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		try {
			zipFile.createSplitZipFileFromFolder(new File("C:\\Users\\admin\\Desktop\\CloudDriver"), params, true,
					1024 * 1024 * 1024 * 2L);

			while (!progressMonitor.getState().equals(ProgressMonitor.State.READY)) {
				System.out.println("Percentage done: " + progressMonitor.getPercentDone());
				System.out.println("Current file: " + progressMonitor.getFileName());
				System.out.println("Current task: " + progressMonitor.getCurrentTask());

				Thread.sleep(100);
			}

//			zipFile.extractAll("C:\\\\Users\\\\admin\\\\Desktop\\\\result");

		} catch (Exception e) {
			e.printStackTrace();
		}

		long interval = (new Date().getTime()) - now.getTime();

		System.out.print("ºÄÊ±£º" + (interval / 1000));

	}

}
