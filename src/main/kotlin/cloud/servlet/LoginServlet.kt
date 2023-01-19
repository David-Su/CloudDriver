package cloud.servlet

import cloud.bean.CodeMessage
import cloud.bean.Response
import cloud.bean.Token
import cloud.manager.logger
import cloud.util.TokenUtil
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet("/login")
class LoginServlet : HttpServlet() {
    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.characterEncoding = "UTF-8"
        val gson = Gson()
        val sb = StringBuilder()
        val reader = BufferedReader(InputStreamReader(req.inputStream))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            sb.append(line)
        }
        val body = gson.fromJson(sb.toString(), JsonObject::class.java)
        val username = body["username"].asString
        val password = body["password"].asString
        if (username != "root" || password != "root") {
            logger.info("账号密码错误：" + gson
                    .toJson(Response<Any?>(CodeMessage.UN_OR_PW_ERROR.code, CodeMessage.UN_OR_PW_ERROR.message, null)))
            resp.writer.write(gson
                    .toJson(Response<Any?>(CodeMessage.UN_OR_PW_ERROR.code, CodeMessage.UN_OR_PW_ERROR.message, null)))
            return
        }
        val token = TokenUtil.getToken(username)
        logger.info("用户：username->$username  password->$password")
        logger.info("登录成功：token->$token")
        resp.writer.write(gson.toJson(Response(CodeMessage.OK.code,
                CodeMessage.OK.message, Token(token))))

    }
}