package cloud;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import cloud.bean.CloudFile;
import cloud.bean.CreateDir;
import cloud.bean.Response;

@WebServlet("/createdir")
public class CreateDirServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		CreateDir params = JsonUtil.fromJsonReader(req.getReader(), CreateDir.class);

		if (params.paths.get(0).equals(Cons.Path.USER_DIR_STUB)) {
			params.paths.remove(0);
		}

		String path = FileUtil.getWholePath(Cons.Path.DATA_DIR, TokenUtil.getUsername(req.getParameter("token")),
				FileUtil.getWholePath(params.paths));

		boolean result = new File(path).mkdirs();

		System.out.print("CreateDirServlet: path->" + path + "\n");

		System.out.print("CreateDirServlet: mkdirs->" + new File(path).mkdirs() + "\n");

		resp.getWriter().write(JsonUtil.toJson(result
				? new Response<Void>(CodeMessage.OK.code, CodeMessage.OK.message, null)
				: new Response<Void>(CodeMessage.CREATE_DIR_FAIL.code, CodeMessage.CREATE_DIR_FAIL.message, null)));

	}
}
