package cloud;

import java.io.File;

public final class FileUtil {

	public static String getWholePath(String... path) {

		StringBuilder wholePath = new StringBuilder("");

		for (int i = 0; i < path.length; i++) {
			wholePath.append(path[i]).append(File.separator);
		}

		return wholePath.toString();
	}
	
	public static void deleteFile(File file) {
		
		if(file.isFile()) {
			file.delete();
			return;
		}
		
		File[] files = file.listFiles();
		
		for(int i = 0;i<files.length;i++) {
			deleteFile(files[i]);
		}
		file.delete();
	}
}
