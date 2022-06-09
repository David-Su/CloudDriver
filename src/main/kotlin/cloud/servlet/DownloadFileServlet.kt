package cloud.servlet

import cloud.config.Cons
import cloud.config.FileUtil
import cloud.util.CloudFileUtil
import cloud.util.TokenUtil
import java.io.*
import java.util.*
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet("/downloadfile")
class DownloadFileServlet : HttpServlet() {
    @Throws(ServletException::class, IOException::class)
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        val filePaths = Base64.getUrlDecoder().decode(request.getParameter("filePaths")).toString(Charsets.UTF_8)
        print("filePaths->$filePaths\n")

        val path = FileUtil.getWholePath(Cons.Path.DATA_DIR, TokenUtil.getUsername(request.getParameter("token")),
                CloudFileUtil.getWholePath(filePaths))

        // 要下载的文件，此处以项目pom.xml文件举例说明。实际项目请根据实际业务场景获取
        val file = File(path)
        print("DownloadFileServlet: path->$path")
        //		File file = new File("C:\\Users\\admin\\Desktop\\video.mkv");

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
        // Content-Type 表示资源类型，如：文件类型
        response.setHeader("Content-Type", contentType)
        // Content-Disposition 表示响应内容以何种形式展示，是以内联的形式（即网页或者页面的一部分），还是以附件的形式下载并保存到本地。
        // 这里文件名换成下载后你想要的文件名，inline表示内联的形式，即：浏览器直接下载
        response.setHeader("Content-Disposition", "attachment;filename=" + file.name)
        // Content-Length 表示资源内容长度，即：文件大小
        response.setHeader("Content-Length", contentLength.toString())
        // Content-Range 表示响应了多少数据，格式为：[要下载的开始位置]-[结束位置]/[文件总大小]
        response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + file.length())
        response.status = HttpServletResponse.SC_OK
        response.contentType = contentType
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