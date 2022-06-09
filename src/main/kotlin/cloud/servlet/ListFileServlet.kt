package cloud.servlet

import cloud.bean.CloudFile
import cloud.bean.CodeMessage
import cloud.bean.Response
import cloud.config.Cons
import cloud.config.FileUtil
import cloud.manager.logger
import cloud.util.JsonUtil
import cloud.util.TokenUtil
import java.io.File
import java.io.IOException
import java.io.Writer
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet("/listfile")
class ListFileServlet : HttpServlet() {
    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val userDir = File(
                FileUtil.getWholePath(Cons.Path.DATA_DIR, TokenUtil.getUsername(req.getParameter("token"))))
        if (!userDir.exists()) {
            userDir.mkdirs()
        }

        logger.info("userDir->$userDir\n")
//        print("userDir->$userDir\n")

//		FileUtil.deleteFile(new File(path));
        val cloudFile = CloudFile()
        cloudFile.name = Cons.Path.USER_DIR_STUB
        cloudFile.children = generateCloudFile(userDir)
        val writer: Writer = resp.writer
        writer.write(
                JsonUtil.toJson(Response(CodeMessage.OK.code, CodeMessage.OK.message, cloudFile)))
    }

    private fun generateCloudFile(file: File): List<CloudFile>? {
        val children = file.listFiles()
        if (children.isEmpty()) return null
        val cloudFiles: MutableList<CloudFile> = ArrayList()
        for (i in children.indices) {
            val child = children[i]
            val cloudFile = CloudFile()
            cloudFile.name = child.name
            cloudFile.isDir = child.isDirectory
            if (cloudFile.isDir) {
                cloudFile.children = generateCloudFile(child)
            }
            cloudFiles.add(cloudFile)
        }
        return cloudFiles
    }
}