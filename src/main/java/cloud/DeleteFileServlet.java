package cloud;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import cloud.bean.CreateDir;

@WebServlet("/deletefile")
public class DeleteFileServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		CreateDir params = JsonUtil.fromJsonStream(req.getInputStream(), CreateDir.class);

		String path = FileUtil.getWholePath(Cons.Path.ROOT_DIR, TokenUtil.getUsername(req.getParameter("token")),
				params.path);

		System.out.print("DeleteFileServlet: path->" + path + "\n");

		FileUtil.deleteFile(new File(path));

	}
}
