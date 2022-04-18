package cloud;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cloud.bean.Response;
import cloud.bean.Token;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding("UTF-8");
		Gson gson = new Gson();
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}

		JsonObject body = gson.fromJson(sb.toString(), JsonObject.class);

		String username = body.get("username").getAsString();

		String password = body.get("password").getAsString();

		if (!username.equals("root") || !password.equals("root")) {

			System.out.print("账号密码错误：" + gson
					.toJson(new Response<>(CodeMessage.TOKEN_ILLEGAL.code, CodeMessage.TOKEN_ILLEGAL.message, null)));

			resp.getWriter().write(gson
					.toJson(new Response<>(CodeMessage.TOKEN_ILLEGAL.code, CodeMessage.TOKEN_ILLEGAL.message, null)));
			return;
		}

		String token = TokenUtil.getToken(username);

		System.out.print("登录成功：token ->" + token + "\n");
		System.out.print("token解析用户名：" + TokenUtil.getUsername(token) + "\n");

		resp.getWriter().write(gson.toJson(new Response<Token>(CodeMessage.TOKEN_ILLEGAL.code,
				CodeMessage.TOKEN_ILLEGAL.message, new Token(token))));

		System.out.print("用户：username->" + username + "  password->" + password + "\n");
	}
}
