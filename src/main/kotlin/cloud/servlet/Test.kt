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
        print("""
    Range==${request.getHeader("Range")}
    
    """.trimIndent())
        val sb = StringBuilder()

        //1. ��ȡ�����̷�
        val roots = File.listRoots()
        for (i in roots.indices) {
            sb.append(roots[i].path + "; ") //����·��
            sb.append((roots[i].totalSpace / 1024 / 1024).toString() + "; ") //�����ܿռ��С
            sb.append((roots[i].usableSpace / 1024 / 1024).toString() + "; ") //ʣ����̿ռ��С
            sb.append(roots[i].freeSpace / 1024 / 1024) //ʣ����̿ռ��С
            sb.append("\n")
        }
        response.writer.write(sb.toString())
    }
}