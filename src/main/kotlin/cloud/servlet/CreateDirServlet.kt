package cloud.servlet

import cloud.bean.CodeMessage
import cloud.bean.CreateDir
import cloud.bean.Response
import cloud.config.Cons
import cloud.manager.logger
import cloud.util.FileUtil
import cloud.util.CloudFileUtil
import cloud.util.JsonUtil
import cloud.util.TokenUtil
import java.io.File
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet("/createdir")
class CreateDirServlet : HttpServlet() {
    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {

        val paths = CloudFileUtil.getWholePath(JsonUtil.fromJsonReader(req.reader, CreateDir::class.java).paths, TokenUtil.getUsername(req.getParameter("token")))

        val path = FileUtil.getWholePath(Cons.Path.DATA_DIR, paths)
        val result = File(path).mkdirs()
        logger.info("CreateDirServlet: path->$path result->$result")

        resp.writer.write(JsonUtil.toJson(if (result) Response<Any?>(CodeMessage.OK.code, CodeMessage.OK.message, null) else Response<Void?>(CodeMessage.CREATE_DIR_FAIL.code, CodeMessage.CREATE_DIR_FAIL.message, null)))
    }
}