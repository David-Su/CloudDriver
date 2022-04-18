package cloud;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import cloud.bean.User;

@WebServlet("/test")
public class Test extends HttpServlet{
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.print("doGet");
		
		//3.得到执行sql的对象SqlSession 
		SqlSession sqlSession = MyBatisManager.getInstance().getSessionFactory().openSession();
		//4.执行sql语句
		List<User> list = sqlSession.selectList("findAllUser");
		//5.处理结果
		for (User user: list) {
			System.out.println("用户名："+user.getUsername()+"，密码："+user.getPassword());
		}
		//6.关闭数据库会话
		sqlSession.close();
	}
}
