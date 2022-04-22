package cloud;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.servlets.DefaultServlet;

public class MyDefaultServlet extends DefaultServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		super.doGet(request, response);
		System.out.print("MyDefaultServlet");
		
		String jvmName = ManagementFactory.getRuntimeMXBean().getName();
		String pid = jvmName.split("@")[0];
		
		File logFile = new File("C:\\Users\\admin\\Desktop\\log.txt");
		FileWriter fos = null;
        if(!logFile.exists()){
        	logFile.createNewFile();//����ļ������ڣ��ʹ������ļ�
            fos = new FileWriter(logFile);//�״�д���ȡ
        }else{
            //����ļ��Ѵ��ڣ���ô�����ļ�ĩβ׷��д��
            fos = new FileWriter(logFile,true);//���ﹹ�췽������һ������true,��ʾ���ļ�ĩβ׷��д��
        }
        fos.append("\n  MyDefaultServlet pid->"+(pid));
        fos.flush();
	}
}
