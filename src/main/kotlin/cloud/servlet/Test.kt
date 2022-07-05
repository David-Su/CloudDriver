package cloud.servlet

import java.io.File
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(name = "test", value = ["/test"])
class Test : HttpServlet() {
    @Throws(ServletException::class, IOException::class)
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        print("doGet")
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
        print("""
    Range==${request.getHeader("Range")}
    
    """.trimIndent())
        val sb = StringBuilder()

        //1. 获取本机盘符
        val roots = File.listRoots()
        for (i in roots.indices) {
            sb.append(roots[i].path + "; ") //磁盘路径
            sb.append((roots[i].totalSpace / 1024 / 1024).toString() + "; ") //磁盘总空间大小
            sb.append((roots[i].usableSpace / 1024 / 1024).toString() + "; ") //剩余磁盘空间大小
            sb.append(roots[i].freeSpace / 1024 / 1024) //剩余磁盘空间大小
            sb.append("\n")
        }
        response.writer.write(sb.toString())
    }
}