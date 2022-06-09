package cloud.filter

import cloud.bean.CodeMessage
import cloud.bean.Response
import cloud.util.TokenUtil
import com.google.gson.Gson
import java.io.IOException
import javax.servlet.*
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebFilter(filterName = "GlobalFilter", urlPatterns = ["/*"])
class GlobalFilter : Filter {
    private val gson = Gson()
    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
    }

    override fun destroy() {}
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpServletRequest = request as HttpServletRequest
        val httpServletResponse = response as HttpServletResponse
        response.setCharacterEncoding("UTF-8")
        request.setCharacterEncoding("UTF-8")

        /* ��������������ַ */httpServletResponse.setHeader("Access-Control-Allow-Origin", "*")
        /* �����������󷽷�GET, POST, HEAD �� */httpServletResponse.setHeader("Access-Control-Allow-Methods", "*")
        /* ����Ԥ�������Ļ���ʱ�� (s) */
//		httpServletResponse.setHeader("Access-Control-Max-Age", "4200");  
        /* ������������ͷ */httpServletResponse.setHeader("Access-Control-Allow-Headers", "*")
        /* �Ƿ�Я��cookie */httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true")
        print("""
    doFilter:${httpServletRequest.requestURL}
    
    """.trimIndent())
        if (httpServletRequest.requestURI.endsWith("/login")) {
            chain.doFilter(request, response)
        } else {
            val token = request.getParameter("token")
            if (token == null || token.isEmpty()) {
                response.getWriter().write(gson.toJson(
                        Response<Any?>(CodeMessage.TOKEN_ILLEGAL.code, CodeMessage.TOKEN_ILLEGAL.message, null)))
                return
            }
            if (TokenUtil.timeout(token)) {
                response.getWriter().write(gson.toJson(
                        Response<Any?>(CodeMessage.TOKEN_TIMEOUT.code, CodeMessage.TOKEN_TIMEOUT.message, null)))
                return
            }
            if (!TokenUtil.vaild(token)) {
                response.getWriter().write(gson.toJson(
                        Response<Any?>(CodeMessage.TOKEN_ILLEGAL.code, CodeMessage.TOKEN_ILLEGAL.message, null)))
                return
            }
            chain.doFilter(request, response)
        }
    }
}