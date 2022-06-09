package cloud.servlet

import cloud.bean.CodeMessage
import cloud.bean.DeleteFile
import cloud.bean.Response
import cloud.config.Cons
import cloud.config.FileUtil
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

@WebServlet("/deletefile")
class DeleteFileServlet : HttpServlet() {
    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val location = CloudFileUtil.getWholePath(JsonUtil.fromJsonReader(req.reader, DeleteFile::class.java).paths)
        val path = FileUtil.getWholePath(Cons.Path.DATA_DIR, TokenUtil.getUsername(req.getParameter("token")), location)
        print("DeleteFileServlet: path->$path\n")
        FileUtil.deleteFile(File(path))
        resp.writer.write(JsonUtil.toJson(Response<Any?>(CodeMessage.OK.code, CodeMessage.OK.message, null)))
    }
}