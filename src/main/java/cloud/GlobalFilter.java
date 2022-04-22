package cloud;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import com.google.gson.Gson;

import cloud.bean.Response;

@WebFilter(filterName = "GlobalFilter", urlPatterns = "/*"/* 通配符（*）表示对所有的web资源进行拦截 */)
public class GlobalFilter implements Filter {

	Gson gson = new Gson();

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;

		System.out.print("doFilter:" + httpServletRequest.getRequestURL()+"\n");

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
        fos.append("\n  doFilter pid->"+(pid));
        fos.flush();
		
		
		if (httpServletRequest.getRequestURI().endsWith("/login")) {
			
			chain.doFilter(request, response);
			
		} else {
			String token = request.getParameter("token");

			if ((token == null || token.isEmpty()) || !TokenUtil.vaild(token)) {
				response.getWriter().write(gson.toJson(
						new Response<>(CodeMessage.TOKEN_ILLEGAL.code, CodeMessage.TOKEN_ILLEGAL.message, null)));
				return;
			}

			if (TokenUtil.timeout(token)) {
				response.setCharacterEncoding(token);
				response.getWriter().write(gson.toJson(
						new Response<>(CodeMessage.TOKEN_TIMEOUT.code, CodeMessage.TOKEN_TIMEOUT.message, null)));
				return;
			}

			chain.doFilter(request, response);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

}
