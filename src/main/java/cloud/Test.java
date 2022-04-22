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

//		//3.得到执行sql的对象SqlSession 
//		SqlSession sqlSession = MyBatisManager.getInstance().getSessionFactory().openSession();
//		//4.执行sql语句
//		List<User> list = sqlSession.selectList("findAllUser");
//		//5.处理结果
//		for (User user: list) {
//			System.out.println("用户名："+user.getUsername()+"，密码："+user.getPassword());
//		}
//		//6.关闭数据库会话
//		sqlSession.close();
//		
//		resp.getWriter().write(getClass().getClassLoader().getResource(".").getPath());

//		System.out.print("doGet 文件大小-》"+ video.length()+"\n");
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
//		if (request.getHeader("Range") != null) // 客户端请求的下载的文件块的开始字节
//		{
//            //如果是下载文件的范围而不是全部,向客户端声明支持并开始文件块下载   
//            //要设置状态   
//            //响应的格式是:   
//            //HTTP/1.1 206 Partial Content   
//            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
//            //从请求中得到开始的字节   
//            //请求的格式是:   
//            //Range: bytes=[文件块的开始字节]-   
//            rangeStart = Long.parseLong((request.getHeader("Range").replaceAll("bytes=", "").split("-")[0]));
//		}
//
//		response.setContentLengthLong(fileLength - rangeStart);
//		
////        if (rangeStart != 0) {
////            //不是从最开始下载,   
////            //响应的格式是:   
////            //Content-Range: bytes [文件块的开始字节]-[文件的总大小 - 1]/[文件的总大小]   
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
//		byte[] bytes = new byte[1024]; // 设置读取文件时的规模，大小
//		int len = 0;
//		while ((len = fileInputStream.read(bytes)) != -1) {
//			os.write(bytes, 0, len); // 将数据写到
//		}
//		fileInputStream.close();
//		os.flush();
//		os.close();

		// 要下载的文件，此处以项目pom.xml文件举例说明。实际项目请根据实际业务场景获取
        File file = new File(System.getProperty("user.dir") + "\\CloudDriver\\"+"video.mkv");

//		File file = new File("C:\\Users\\admin\\Desktop\\video.mkv");

		// 开始下载位置
		long startByte = 0;
		// 结束下载位置
		long endByte = file.length() - 1;

		String range = request.getHeader("Range");

		// 有range的话
		if (range != null && range.contains("bytes=") && range.contains("-")) {
			range = range.substring(range.lastIndexOf("=") + 1).trim();
			String ranges[] = range.split("-");
			try {
				// 根据range解析下载分片的位置区间
				if (ranges.length == 1) {
					// 情况1，如：bytes=-1024 从开始字节到第1024个字节的数据
					if (range.startsWith("-")) {
						endByte = Long.parseLong(ranges[0]);
					}
					// 情况2，如：bytes=1024- 第1024个字节到最后字节的数据
					else if (range.endsWith("-")) {
						startByte = Long.parseLong(ranges[0]);
					}
				}
				// 情况3，如：bytes=1024-2048 第1024个字节到2048个字节的数据
				else if (ranges.length == 2) {
					startByte = Long.parseLong(ranges[0]);
					endByte = Long.parseLong(ranges[1]);
				}

			} catch (NumberFormatException e) {
				startByte = 0;
				endByte = file.length() - 1;
			}
		}

		// 要下载的长度
		long contentLength = endByte - startByte + 1;
		// 文件名
		String fileName = file.getName();
		// 文件类型
		String contentType = request.getServletContext().getMimeType(fileName);

		// 响应头设置
		// https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Accept-Ranges
		response.setHeader("Accept-Ranges", "bytes");
		// Content-Type 表示资源类型，如：文件类型
		response.setHeader("Content-Type", contentType);
		// Content-Disposition 表示响应内容以何种形式展示，是以内联的形式（即网页或者页面的一部分），还是以附件的形式下载并保存到本地。
		// 这里文件名换成下载后你想要的文件名，inline表示内联的形式，即：浏览器直接下载
		response.setHeader("Content-Disposition", "inline;filename="+file.getName());
		// Content-Length 表示资源内容长度，即：文件大小
		response.setHeader("Content-Length", String.valueOf(contentLength));
		// Content-Range 表示响应了多少数据，格式为：[要下载的开始位置]-[结束位置]/[文件总大小]
		response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + file.length());

		response.setStatus(response.SC_OK);
		response.setContentType(contentType);

		BufferedOutputStream outputStream = null;
		RandomAccessFile randomAccessFile = null;
		// 已传送数据大小
		long transmitted = 0;
		try {
			randomAccessFile = new RandomAccessFile(file, "r");
			outputStream = new BufferedOutputStream(response.getOutputStream());
			byte[] buff = new byte[2048];
			int len = 0;
			randomAccessFile.seek(startByte);
			// 判断是否到了最后不足2048（buff的length）个byte
			while ((transmitted + len) <= contentLength && (len = randomAccessFile.read(buff)) != -1) {
				outputStream.write(buff, 0, len);
				transmitted += len;
			}
			// 处理不足buff.length部分
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
