package cloud.servlet

import cloud.model.net.CodeMessage
import cloud.model.net.CreateDir
import cloud.model.net.Response
import cloud.config.Cons
import cloud.manager.logger
import cloud.util.FileUtil
import cloud.util.CloudFileUtil
import cloud.util.JsonUtil
import cloud.util.TokenUtil
import java.io.File
import java.io.IOException
import jakarta.servlet.ServletException
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@WebServlet("/createdir")
class CreateDirServlet : HttpServlet() {
    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {

        val paths = CloudFileUtil.getWholePath(JsonUtil.fromJsonReader(req.reader, CreateDir::class.java).paths, TokenUtil.getUsername(req.getParameter("token")))

        val path = FileUtil.getWholePath(Cons.Path.DATA_DIR, paths)

        val file = File(path)

        val response = if (file.exists()) {
            Response<Any?>(CodeMessage.DIR_ALREADY_EXIST.code, CodeMessage.DIR_ALREADY_EXIST.message, null)
        } else if (file.mkdirs()) {
            Response<Any?>(CodeMessage.OK.code, CodeMessage.OK.message, null)
        } else {
            Response<Void?>(CodeMessage.CREATE_DIR_FAIL.code, CodeMessage.CREATE_DIR_FAIL.message, null)
        }

        logger.info {
            "file:${file}"
        }

        resp.writer.write(JsonUtil.toJson(response))
    }
}