package cloud;

import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import cloud.bean.CloudFile;
import cloud.bean.CreateDir;

@WebServlet("/listfile")
public class ListFileServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		File userDir = new File(
				FileUtil.getWholePath(Cons.Path.ROOT_DIR, TokenUtil.getUsername(req.getParameter("token"))));

		if (!userDir.exists()) {
			userDir.mkdirs();
		}

		System.out.print("DeleteFileServlet: userDir->" + userDir + "\n");

//		FileUtil.deleteFile(new File(path));

		CloudFile cloudFile = new CloudFile();
		cloudFile.name = File.separator;
		cloudFile.children = generateCloudFile(userDir);
		
		resp.getWriter().write(JsonUtil.toJson(cloudFile));
	}

	private List<CloudFile> generateCloudFile(File file) {
		File[] children = file.listFiles();
		if (children.length == 0)
			return null;
		List<CloudFile> cloudFiles = new ArrayList<CloudFile>();
		for (int i = 0; i < children.length; i++) {
			File child = children[i];

			CloudFile cloudFile = new CloudFile();
			cloudFile.name = child.getName();
			cloudFile.isDir = child.isDirectory();

			if (cloudFile.isDir) {
				cloudFile.children = generateCloudFile(child);
			}
			cloudFiles.add(cloudFile);
		}
		return cloudFiles;
	}
}
