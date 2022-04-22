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
        	logFile.createNewFile();//如果文件不存在，就创建该文件
            fos = new FileWriter(logFile);//首次写入获取
        }else{
            //如果文件已存在，那么就在文件末尾追加写入
            fos = new FileWriter(logFile,true);//这里构造方法多了一个参数true,表示在文件末尾追加写入
        }
        fos.append("\n  MyDefaultServlet pid->"+(pid));
        fos.flush();
	}
}
