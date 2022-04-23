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
