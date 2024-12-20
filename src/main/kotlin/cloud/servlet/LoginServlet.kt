package cloud.servlet

import cloud.config.Cons
import cloud.model.net.CodeMessage
import cloud.model.net.Response
import cloud.model.net.Token
import cloud.manager.logger
import cloud.util.FileUtil
import cloud.util.TokenUtil
import com.google.common.eventbus.EventBus
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import jakarta.servlet.ServletException
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.File

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

        synchronized(username.intern()) {
            if (username != "root" || password != "root") {
                resp.writer.write(
                    gson
                        .toJson(
                            Response<Any?>(
                                CodeMessage.UN_OR_PW_ERROR.code,
                                CodeMessage.UN_OR_PW_ERROR.message,
                                null
                            )
                        )
                )
                return
            }
            val token = TokenUtil.generateToken(username)
            logger.info {
                buildString {
                    append("\n")
                    append("username->$username")
                    append("\n")
                    append("password->$password")
                    append("\n")
                    append("token->$token")
                }
            }
            //创建用户的数据文件夹
            File(FileUtil.getWholePath(Cons.Path.DATA_DIR, username))
                .mkdirs()
            resp.writer.write(
                gson.toJson(
                    Response(
                        CodeMessage.OK.code,
                        CodeMessage.OK.message, Token(token)
                    )
                )
            )
        }

    }
}