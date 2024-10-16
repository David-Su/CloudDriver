package cloud.servlet

import cloud.config.Cons
import cloud.manager.logger
import cloud.util.FileUtil
import cloud.util.CloudFileUtil
import cloud.util.JsonUtil
import cloud.util.TokenUtil
import com.google.gson.reflect.TypeToken
import java.io.*
import java.util.*
import jakarta.servlet.ServletException
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@WebServlet("/downloadfile")
class DownloadFileServlet : HttpServlet() {

    companion object {
        const val FILE_TYPE_DATA = 1
        const val FILE_TYPE_TEMP_PREVIEW = 2

        //下载模式
        const val DOWNLOAD_MODE_DOWNLOAD = 1
        const val DOWNLOAD_MODE_PLAY_ONLINE = 2
    }

    @Throws(ServletException::class, IOException::class)
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        val filePaths = Base64
            .getUrlDecoder()
            .decode(request.getParameter("filePaths"))
            .toString(Charsets.UTF_8)
            .let {
                logger.info {
                    "filePaths json:$it"
                }
                JsonUtil.gson.fromJson<List<String>>(it, (object : TypeToken<List<String>>() {}).type)
            }
        val fileType = request.getParameter("fileType")?.toInt() ?: FILE_TYPE_DATA
        val downloadMode = request.getParameter("downloadMode")?.toInt() ?: DOWNLOAD_MODE_DOWNLOAD
        logger.info {
            "filePaths:$filePaths"
        }

        val dir = when (fileType) {
            FILE_TYPE_TEMP_PREVIEW -> Cons.Path.TEMP_PREVIEW_DIR
            else -> Cons.Path.DATA_DIR
        }

        val path = FileUtil.getWholePath(
            dir,
            CloudFileUtil.getWholePath(
                filePaths,
                TokenUtil.getUsername(request.getParameter("token"))
            )
        )

        // 要下载的文件，此处以项目pom.xml文件举例说明。实际项目请根据实际业务场景获取
        val file = File(path)

        logger.info {
            "path:$path"
        }

        // 开始下载位置
        var startByte: Long = 0
        // 结束下载位置
        var endByte = file.length() - 1
        var range = request.getHeader("Range")

        // 有range的话
        if (range != null && range.contains("bytes=") && range.contains("-")) {
            range = range.substring(range.lastIndexOf("=") + 1).trim { it <= ' ' }
            val ranges = range.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            try {
                // 根据range解析下载分片的位置区间
                if (ranges.size == 1) {
                    // 情况1，如：bytes=-1024 从开始字节到第1024个字节的数据
                    if (range.startsWith("-")) {
                        endByte = ranges[0].toLong()
                    } else if (range.endsWith("-")) {
                        startByte = ranges[0].toLong()
                    }
                } else if (ranges.size == 2) {
                    startByte = ranges[0].toLong()
                    endByte = ranges[1].toLong()
                }
            } catch (e: NumberFormatException) {
                startByte = 0
                endByte = file.length() - 1
            }
        }

        // 要下载的长度
        val contentLength = endByte - startByte + 1
        // 文件名
        val fileName = file.name
        // 文件类型
        val contentType = request.servletContext.getMimeType(fileName)

        // 响应头设置
        // https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Accept-Ranges
        response.setHeader("Accept-Ranges", "bytes")
        // Content-Length 表示资源内容长度，即：文件大小
        response.setHeader("Content-Length", contentLength.toString())
        // Content-Range 表示响应了多少数据，格式为：[要下载的开始位置]-[结束位置]/[文件总大小]
        response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + file.length())
        response.contentType = contentType

        when (downloadMode) {
            DOWNLOAD_MODE_DOWNLOAD -> {
                // Content-Disposition 表示响应内容以何种形式展示，是以内联的形式（即网页或者页面的一部分），还是以附件的形式下载并保存到本地。
                // 这里文件名换成下载后你想要的文件名，inline表示内联的形式，即：浏览器直接下载
                response.setHeader("Content-Disposition", "attachment;filename=\"${fileName}\"")
                //表示服务器返回了请求的完整资源
                response.status = HttpServletResponse.SC_OK
            }

            DOWNLOAD_MODE_PLAY_ONLINE -> {
                //表示服务器成功处理了部分请求，并返回了部分资源
                response.status = HttpServletResponse.SC_PARTIAL_CONTENT
            }
        }


        var outputStream: BufferedOutputStream? = null
        var randomAccessFile: RandomAccessFile? = null
        // 已传送数据大小
        var transmitted: Long = 0
        try {
            randomAccessFile = RandomAccessFile(file, "r")
            outputStream = BufferedOutputStream(response.outputStream)
            val buff = ByteArray(2048)
            var len = 0
            randomAccessFile.seek(startByte)
            // 判断是否到了最后不足2048（buff的length）个byte
            while (transmitted + len <= contentLength && randomAccessFile.read(buff).also { len = it } != -1) {
                outputStream.write(buff, 0, len)
                transmitted += len.toLong()
            }
            // 处理不足buff.length部分
            if (transmitted < contentLength) {
                len = randomAccessFile.read(buff, 0, (contentLength - transmitted).toInt())
                outputStream.write(buff, 0, len)
                transmitted += len.toLong()
            }
            outputStream.flush()
            response.flushBuffer()
            randomAccessFile.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                randomAccessFile?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}