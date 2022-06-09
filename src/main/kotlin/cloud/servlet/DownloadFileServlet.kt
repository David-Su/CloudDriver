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

        // Ҫ���ص��ļ����˴�����Ŀpom.xml�ļ�����˵����ʵ����Ŀ�����ʵ��ҵ�񳡾���ȡ
        val file = File(path)
        print("DownloadFileServlet: path->$path")
        //		File file = new File("C:\\Users\\admin\\Desktop\\video.mkv");

        // ��ʼ����λ��
        var startByte: Long = 0
        // ��������λ��
        var endByte = file.length() - 1
        var range = request.getHeader("Range")

        // ��range�Ļ�
        if (range != null && range.contains("bytes=") && range.contains("-")) {
            range = range.substring(range.lastIndexOf("=") + 1).trim { it <= ' ' }
            val ranges = range.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            try {
                // ����range�������ط�Ƭ��λ������
                if (ranges.size == 1) {
                    // ���1���磺bytes=-1024 �ӿ�ʼ�ֽڵ���1024���ֽڵ�����
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

        // Ҫ���صĳ���
        val contentLength = endByte - startByte + 1
        // �ļ���
        val fileName = file.name
        // �ļ�����
        val contentType = request.servletContext.getMimeType(fileName)

        // ��Ӧͷ����
        // https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Accept-Ranges
        response.setHeader("Accept-Ranges", "bytes")
        // Content-Type ��ʾ��Դ���ͣ��磺�ļ�����
        response.setHeader("Content-Type", contentType)
        // Content-Disposition ��ʾ��Ӧ�����Ժ�����ʽչʾ��������������ʽ������ҳ����ҳ���һ���֣��������Ը�������ʽ���ز����浽���ء�
        // �����ļ����������غ�����Ҫ���ļ�����inline��ʾ��������ʽ�����������ֱ������
        response.setHeader("Content-Disposition", "attachment;filename=" + file.name)
        // Content-Length ��ʾ��Դ���ݳ��ȣ������ļ���С
        response.setHeader("Content-Length", contentLength.toString())
        // Content-Range ��ʾ��Ӧ�˶������ݣ���ʽΪ��[Ҫ���صĿ�ʼλ��]-[����λ��]/[�ļ��ܴ�С]
        response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + file.length())
        response.status = HttpServletResponse.SC_OK
        response.contentType = contentType
        var outputStream: BufferedOutputStream? = null
        var randomAccessFile: RandomAccessFile? = null
        // �Ѵ������ݴ�С
        var transmitted: Long = 0
        try {
            randomAccessFile = RandomAccessFile(file, "r")
            outputStream = BufferedOutputStream(response.outputStream)
            val buff = ByteArray(2048)
            var len = 0
            randomAccessFile.seek(startByte)
            // �ж��Ƿ��������2048��buff��length����byte
            while (transmitted + len <= contentLength && randomAccessFile.read(buff).also { len = it } != -1) {
                outputStream.write(buff, 0, len)
                transmitted += len.toLong()
            }
            // ������buff.length����
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