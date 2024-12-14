package cloud.servlet

import cloud.model.net.CodeMessage
import cloud.model.net.Response
import cloud.config.Cons
import cloud.manager.logger
import cloud.model.net.RenameFile
import cloud.util.*
import java.io.File
import java.io.IOException
import jakarta.servlet.ServletException
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@WebServlet("/renamefile")
class RenameFileServlet : HttpServlet() {
    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {

        val params = JsonUtil.fromJsonReader(req.reader, RenameFile::class.java)

        val paths = CloudFileUtil.getWholePath(params.paths, TokenUtil.getUsername(req.getParameter("token")))
        val newPaths = CloudFileUtil.getWholePath(params.newPaths, TokenUtil.getUsername(req.getParameter("token")))

        val sourceFile = File(FileUtil.getWholePath(Cons.Path.DATA_DIR, paths))
        val newFile = File(FileUtil.getWholePath(Cons.Path.DATA_DIR, newPaths))

        logger.info { "sourceFile:$sourceFile" }
        logger.info { "newFile:$newFile" }

        if (!sourceFile.exists() || newFile.exists()) {
            resp.writer.write(
                JsonUtil.toJson(
                    Response<Any?>(
                        CodeMessage.DIR_OR_FILE_ALREADY_EXIST.code,
                        CodeMessage.DIR_OR_FILE_ALREADY_EXIST.message,
                        null
                    )
                )
            )
            return
        }

        val sourcePreviewFile: File? = if (sourceFile.isFile) PreviewFileUtil.getPreviewFile(
            sourceFile,
            File(Cons.Path.DATA_DIR)
        ) else PreviewFileUtil.getPreviewParentFile(sourceFile, File(Cons.Path.DATA_DIR))
        logger.info { "sourcePreviewFile:$sourcePreviewFile" }

        if (!sourceFile.renameTo(newFile)) {
            resp.writer.write(
                JsonUtil.toJson(
                    Response<Any?>(
                        CodeMessage.RENAME_FILE_FAIL.code,
                        CodeMessage.RENAME_FILE_FAIL.message,
                        null
                    )
                )
            )
            return
        }

        /**
         * 来到这里可以保证newFile已存在而且sourceFile和newFile同为文件或文件夹
         */

        if (sourcePreviewFile != null && sourcePreviewFile.exists()) {

            val newPreviewFileParent = PreviewFileUtil.getPreviewParentFile(newFile, File(Cons.Path.DATA_DIR))

            if (sourcePreviewFile.isFile) { //sourceFile是文件的情况

                val newPreviewFile = File(
                    newPreviewFileParent,
                    "${newFile.name}.${sourcePreviewFile.extension}"
                )

                if (newPreviewFile.exists()) {
                    FileUtil.deleteFile(newPreviewFile)
                } else {
                    newPreviewFile.parentFile.mkdirs()
                }

                val result = sourcePreviewFile.renameTo(newPreviewFile)

                logger.info {
                    buildString {
                        append("\n")
                        append("sourceFile是文件")
                        append("\n")
                        append("sourcePreviewFile:${sourcePreviewFile}")
                        append("\n")
                        append("newPreviewFile:${newPreviewFile}")
                        append("\n")
                        append("result:${result}")
                    }
                }
            } else if (sourcePreviewFile.isDirectory) { //sourceFile是文件夹的情况
                if (newPreviewFileParent.exists()) {
                    FileUtil.deleteFile(newPreviewFileParent)
                } else {
                    newPreviewFileParent.parentFile.mkdirs()
                }

                val result = sourcePreviewFile.renameTo(newPreviewFileParent)

                logger.info {
                    buildString {
                        append("\n")
                        append("sourceFile是文件夹")
                        append("\n")
                        append("sourcePreviewFile:${sourcePreviewFile}")
                        append("\n")
                        append("newPreviewFileParent:${newPreviewFileParent}")
                        append("\n")
                        append("result:${result}")
                    }
                }
            }

        }

        resp.writer.write(JsonUtil.toJson(Response<Any?>(CodeMessage.OK.code, CodeMessage.OK.message, null)))
    }
}