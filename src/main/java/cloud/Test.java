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

		StringBuilder sb =new  StringBuilder();

		//1. ��ȡ�����̷�
		File[] roots = File.listRoots();
		for (int i = 0; i < roots.length; i++) {
			sb.append(roots[i].getPath()+"; ");//����·��
			sb.append(roots[i].getTotalSpace()/1024/1024+"; ");//�����ܿռ��С
			sb.append(roots[i].getUsableSpace()/1024/1024+"; ");//ʣ����̿ռ��С
			sb.append(roots[i].getFreeSpace()/1024/1024);//ʣ����̿ռ��С
			sb.append("\n");
		}


		response.getWriter().write(sb.toString());
		
	}

}
