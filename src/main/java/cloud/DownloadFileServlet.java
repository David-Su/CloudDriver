package cloud;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/downloadfile")
public class DownloadFileServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		List<String> paths = new ArrayList<String>(Arrays.asList(request.getParameter("filePaths").split(",")));

		if (paths.get(0).equals(Cons.Path.USER_DIR_STUB)) {
			paths.remove(0);
		}

		String path = FileUtil.getWholePath(Cons.Path.DATA_DIR, TokenUtil.getUsername(request.getParameter("token")),
				FileUtil.getWholePath(paths));

		// Ҫ���ص��ļ����˴�����Ŀpom.xml�ļ�����˵����ʵ����Ŀ�����ʵ��ҵ�񳡾���ȡ
		File file = new File(path);

		System.out.print("DownloadFileServlet: path->" + path);
//		File file = new File("C:\\Users\\admin\\Desktop\\video.mkv");

		// ��ʼ����λ��
		long startByte = 0;
		// ��������λ��
		long endByte = file.length() - 1;

		String range = request.getHeader("Range");

		// ��range�Ļ�
		if (range != null && range.contains("bytes=") && range.contains("-")) {
			range = range.substring(range.lastIndexOf("=") + 1).trim();
			String ranges[] = range.split("-");
			try {
				// ����range�������ط�Ƭ��λ������
				if (ranges.length == 1) {
					// ���1���磺bytes=-1024 �ӿ�ʼ�ֽڵ���1024���ֽڵ�����
					if (range.startsWith("-")) {
						endByte = Long.parseLong(ranges[0]);
					}
					// ���2���磺bytes=1024- ��1024���ֽڵ�����ֽڵ�����
					else if (range.endsWith("-")) {
						startByte = Long.parseLong(ranges[0]);
					}
				}
				// ���3���磺bytes=1024-2048 ��1024���ֽڵ�2048���ֽڵ�����
				else if (ranges.length == 2) {
					startByte = Long.parseLong(ranges[0]);
					endByte = Long.parseLong(ranges[1]);
				}

			} catch (NumberFormatException e) {
				startByte = 0;
				endByte = file.length() - 1;
			}
		}

		// Ҫ���صĳ���
		long contentLength = endByte - startByte + 1;
		// �ļ���
		String fileName = file.getName();
		// �ļ�����
		String contentType = request.getServletContext().getMimeType(fileName);

		// ��Ӧͷ����
		// https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Accept-Ranges
		response.setHeader("Accept-Ranges", "bytes");
		// Content-Type ��ʾ��Դ���ͣ��磺�ļ�����
		response.setHeader("Content-Type", contentType);
		// Content-Disposition ��ʾ��Ӧ�����Ժ�����ʽչʾ��������������ʽ������ҳ����ҳ���һ���֣��������Ը�������ʽ���ز����浽���ء�
		// �����ļ����������غ�����Ҫ���ļ�����inline��ʾ��������ʽ�����������ֱ������
		response.setHeader("Content-Disposition", "attachment;filename=" + file.getName());
		// Content-Length ��ʾ��Դ���ݳ��ȣ������ļ���С
		response.setHeader("Content-Length", String.valueOf(contentLength));
		// Content-Range ��ʾ��Ӧ�˶������ݣ���ʽΪ��[Ҫ���صĿ�ʼλ��]-[����λ��]/[�ļ��ܴ�С]
		response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + file.length());

		response.setStatus(response.SC_OK);
		response.setContentType(contentType);

		BufferedOutputStream outputStream = null;
		RandomAccessFile randomAccessFile = null;
		// �Ѵ������ݴ�С
		long transmitted = 0;
		try {
			randomAccessFile = new RandomAccessFile(file, "r");
			outputStream = new BufferedOutputStream(response.getOutputStream());
			byte[] buff = new byte[2048];
			int len = 0;
			randomAccessFile.seek(startByte);
			// �ж��Ƿ��������2048��buff��length����byte
			while ((transmitted + len) <= contentLength && (len = randomAccessFile.read(buff)) != -1) {
				outputStream.write(buff, 0, len);
				transmitted += len;
			}
			// ������buff.length����
			if (transmitted < contentLength) {
				len = randomAccessFile.read(buff, 0, (int) (contentLength - transmitted));
				outputStream.write(buff, 0, len);
				transmitted += len;
			}

			outputStream.flush();
			response.flushBuffer();
			randomAccessFile.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (randomAccessFile != null) {
					randomAccessFile.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
