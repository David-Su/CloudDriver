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
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import cloud.bean.Response;

@WebFilter(filterName = "GlobalFilter", urlPatterns = "/*"/* 通配符（*）表示对所有的web资源进行拦截 */)
public class GlobalFilter implements Filter {

	private Gson gson = new Gson();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}
	
	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		
		/* 允许跨域的主机地址 */
		httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");  
		/* 允许跨域的请求方法GET, POST, HEAD 等 */
		httpServletResponse.setHeader("Access-Control-Allow-Methods", "*");  
		/* 重新预检验跨域的缓存时间 (s) */
//		httpServletResponse.setHeader("Access-Control-Max-Age", "4200");  
		/* 允许跨域的请求头 */
		httpServletResponse.setHeader("Access-Control-Allow-Headers", "*");  
		/* 是否携带cookie */
		httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");  

		
		System.out.print("doFilter:" + httpServletRequest.getRequestURL()+"\n");
		
		if (httpServletRequest.getRequestURI().endsWith("/login")) {
			
			chain.doFilter(request, response);
			
		} else {
			String token = request.getParameter("token");

			if ((token == null || token.isEmpty())) {
				response.getWriter().write(gson.toJson(
						new Response<>(CodeMessage.TOKEN_ILLEGAL.code, CodeMessage.TOKEN_ILLEGAL.message, null)));
				return;
			}

			if (TokenUtil.timeout(token)) {
				response.getWriter().write(gson.toJson(
						new Response<>(CodeMessage.TOKEN_TIMEOUT.code, CodeMessage.TOKEN_TIMEOUT.message, null)));
				return;
			}

			if (!TokenUtil.vaild(token)) {
				response.getWriter().write(gson.toJson(
						new Response<>(CodeMessage.TOKEN_ILLEGAL.code, CodeMessage.TOKEN_ILLEGAL.message, null)));
				return;
			}
			
			chain.doFilter(request, response);
		}
	}

}
