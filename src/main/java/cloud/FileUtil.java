package cloud;

import java.io.File;
import java.util.List;

public final class FileUtil {

	public static String getWholePath(List<String> paths) {
		return getWholePath(paths.toArray(new String[paths.size()]));
	}
	
	public static String getWholePath(String... paths) {

		StringBuilder wholePath = new StringBuilder("");

		for (int i = 0; i < paths.length; i++) {
			if (i > 0) {
				wholePath.append(File.separator);
			}
			wholePath.append(paths[i]);
		}

		return wholePath.toString();
	}

	public static void deleteFile(File file) {

		if (file.isFile()) {
			file.delete();
			return;
		}

		File[] files = file.listFiles();

		for (int i = 0; i < files.length; i++) {
			deleteFile(files[i]);
		}
		file.delete();
	}
}
