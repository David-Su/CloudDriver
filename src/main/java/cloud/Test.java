package cloud;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.servlets.DefaultServlet;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import cloud.bean.User;

@WebServlet("/test")
public class Test extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.print("doGet");

//		//3.�õ�ִ��sql�Ķ���SqlSession 
//		SqlSession sqlSession = MyBatisManager.getInstance().getSessionFactory().openSession();
//		//4.ִ��sql���
//		List<User> list = sqlSession.selectList("findAllUser");
//		//5.������
//		for (User user: list) {
//			System.out.println("�û�����"+user.getUsername()+"�����룺"+user.getPassword());
//		}
//		//6.�ر����ݿ�Ự
//		sqlSession.close();
//		
//		resp.getWriter().write(getClass().getClassLoader().getResource(".").getPath());

//		System.out.print("doGet �ļ���С-��"+ video.length()+"\n");
		System.out.print("Range==" + request.getHeader("Range") + "\n");

//		File video = new File("C:\\Users\\admin\\Desktop\\video.mkv");
//
//		response.setContentType(getServletContext().getMimeType(video.getAbsolutePath()));
//		response.setHeader("content-Disposition", "attachment;filename=" + video.getName());
//		response.setHeader("Accept-Ranges", "bytes");
////		response.setContentType("application/x-download");
//		response.setCharacterEncoding("utf-8");
////		response.setHeader("Access-Control-Allow-Origin", "*");
//		
//		long fileLength = video.length();
//		long rangeStart = 0;
//
//		if (request.getHeader("Range") != null) // �ͻ�����������ص��ļ���Ŀ�ʼ�ֽ�
//		{
//            //����������ļ��ķ�Χ������ȫ��,��ͻ�������֧�ֲ���ʼ�ļ�������   
//            //Ҫ����״̬   
//            //��Ӧ�ĸ�ʽ��:   
//            //HTTP/1.1 206 Partial Content   
//            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
//            //�������еõ���ʼ���ֽ�   
//            //����ĸ�ʽ��:   
//            //Range: bytes=[�ļ���Ŀ�ʼ�ֽ�]-   
//            rangeStart = Long.parseLong((request.getHeader("Range").replaceAll("bytes=", "").split("-")[0]));
//		}
//
//		response.setContentLengthLong(fileLength - rangeStart);
//		
////        if (rangeStart != 0) {
////            //���Ǵ��ʼ����,   
////            //��Ӧ�ĸ�ʽ��:   
////            //Content-Range: bytes [�ļ���Ŀ�ʼ�ֽ�]-[�ļ����ܴ�С - 1]/[�ļ����ܴ�С]   
////            response.setHeader("Content-Range", "bytes " + rangeStart+ "-" + (fileLength-1) + "/" + fileLength);
////        } else {
////            response.setHeader("Content-Range", "bytes " + 0 + "-" + 100 + "/" + fileLength);
////        }
//	
//        response.setHeader("Content-Range", "bytes " + rangeStart+ "-" + fileLength + "/" + fileLength);
//
//
//		FileInputStream fileInputStream = new FileInputStream(video);
//		fileInputStream.skip(rangeStart);
//		OutputStream os = response.getOutputStream();
//		byte[] bytes = new byte[1024]; // ���ö�ȡ�ļ�ʱ�Ĺ�ģ����С
//		int len = 0;
//		while ((len = fileInputStream.read(bytes)) != -1) {
//			os.write(bytes, 0, len); // ������д��
//		}
//		fileInputStream.close();
//		os.flush();
//		os.close();

		// Ҫ���ص��ļ����˴�����Ŀpom.xml�ļ�����˵����ʵ����Ŀ�����ʵ��ҵ�񳡾���ȡ
        File file = new File(System.getProperty("user.dir") + "\\CloudDriver\\"+"video.mkv");

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
		response.setHeader("Content-Disposition", "inline;filename="+file.getName());
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
