package cloud;

import java.io.File;

import java.io.IOException;
import java.util.function.Consumer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import cloud.bean.CreateDir;
import cloud.bean.DeleteFile;
import cloud.bean.Response;

@WebServlet("/deletefile")
public class DeleteFileServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		DeleteFile params = JsonUtil.fromJsonStream(req.getInputStream(), DeleteFile.class);

		if(params.paths.get(0).equals(Cons.Path.USER_DIR_STUB)) {
			params.paths.remove(0);
		}

		String path = FileUtil.getWholePath(Cons.Path.DATA_DIR, TokenUtil.getUsername(req.getParameter("token")),
				FileUtil.getWholePath(params.paths));

		System.out.print("DeleteFileServlet: path->" + path + "\n");

		FileUtil.deleteFile(new File(path));

		resp.getWriter().write(JsonUtil.toJson(new Response<>(CodeMessage.OK.code, CodeMessage.OK.message, null)));

	}
}
