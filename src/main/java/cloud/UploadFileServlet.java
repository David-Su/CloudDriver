package cloud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;

import com.google.gson.Gson;

import cloud.bean.CreateDir;

@WebServlet("/uploadfile")
public class UploadFileServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {

		ArrayList<String> pathArray = new ArrayList<>(Arrays.asList(request.getParameter("path").split(",")));

		if(!pathArray.isEmpty() && pathArray.get(0).equals(Cons.Path.USER_DIR_STUB)) {
			pathArray.remove(0);
		}
		
		String path = FileUtil.getWholePath(pathArray);

		String realPath = FileUtil.getWholePath(Cons.Path.DATA_DIR,
				TokenUtil.getUsername(request.getParameter("token")), path);

		String tempPath = FileUtil.getWholePath(Cons.Path.TEMP_UPLOAD_DIR,
				TokenUtil.getUsername(request.getParameter("token")), path);

		System.out.print("UploadFileServlet: realPath->" + realPath + "\n");
		System.out.print("UploadFileServlet: tempPath->" + tempPath + "\n");

		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());

		List<FileItem> list = upload.parseRequest(new ServletRequestContext(request));

		for (FileItem item : list) {

			File realFile = new File(FileUtil.getWholePath(realPath, item.getName()));
			File tempFile = new File(FileUtil.getWholePath(tempPath, item.getName()));

			System.out.print("UploadFileServlet: tempFile->" + tempFile + "\n");
			System.out.print("UploadFileServlet: realFile->" + realFile + "\n");

			if (realFile.exists()) {
				realFile.delete();
			}
			if (!realFile.getParentFile().exists()) {
				realFile.getParentFile().mkdirs();
			}
			realFile.createNewFile();

			if (!tempFile.exists()) {
				if (!tempFile.getParentFile().exists()) {
					tempFile.getParentFile().mkdirs();
				}
				tempFile.createNewFile();
			}

			// TODO ��ʱ��֧�ֶϵ��ϴ�
			else {
				tempFile.delete();
				tempFile.createNewFile();
			}
			// TODO ��ʱ��֧�ֶϵ��ϴ�

			long skip = tempFile.length();

			System.out.print("UploadFileServlet: skip->" + skip + "\n");

			InputStream is = item.getInputStream();
			is.skip(skip);

			RandomAccessFile randomTempFile = new RandomAccessFile(tempFile, "rw");
			randomTempFile.seek(skip);

			byte[] buffer = new byte[1024];
			int len = 0;

			while ((len = is.read(buffer)) > 0) {
//				System.out.print("UploadFileServlet: read->" + len + "\n");
				randomTempFile.write(buffer, 0, len);
			}

			System.out.print("UploadFileServlet: randomTempFile.length()->" + randomTempFile.length() + "\n");

			is.close();
			randomTempFile.close();

			// ����ʱ�ļ����Ƶ���ʽ·��
			FileInputStream fis = new FileInputStream(tempFile);
			FileOutputStream fos = new FileOutputStream(realFile);
			while ((len = fis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
			fis.close();
			fos.close();
			// ��ʱ�ļ����꣬ɾ��
			tempFile.delete();
		}

	}
}
