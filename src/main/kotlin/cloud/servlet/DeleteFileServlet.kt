package cloud.servlet

import cloud.model.net.CodeMessage
import cloud.model.net.DeleteFile
import cloud.model.net.Response
import cloud.config.Cons
import cloud.manager.logger
import cloud.util.*
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
        val location = CloudFileUtil.getWholePath(JsonUtil.fromJsonReader(req.reader, DeleteFile::class.java).paths, TokenUtil.getUsername(req.getParameter("token")))
        val path = FileUtil.getWholePath(Cons.Path.DATA_DIR, location)
        logger.info("DeleteFileServlet: path->$path\n")
        val dataFile = File(path)
        //先删除预览图
        if (dataFile.isDirectory) {
            FileUtil.deleteFile(PreviewFileUtil.getPreviewParentFile(dataFile,File(Cons.Path.DATA_DIR)))
        } else {
            PreviewFileUtil
                    .getPreviewFile(dataFile, File(Cons.Path.DATA_DIR))
                    ?.also {
                        logger.info("DeleteFileServlet: PreviewFile->${it.absolutePath}")
                        FileUtil.deleteFile(it)
                    }
        }
        //整个文件夹或文件删除
        FileUtil.deleteFile(dataFile)

        resp.writer.write(JsonUtil.toJson(Response<Any?>(CodeMessage.OK.code, CodeMessage.OK.message, null)))
    }
}