package cloud.filter

import cloud.bean.CodeMessage
import cloud.bean.Response
import cloud.manager.logger
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

        /* 允许跨域的主机地址 */httpServletResponse.setHeader("Access-Control-Allow-Origin", "*")
        /* 允许跨域的请求方法GET, POST, HEAD 等 */httpServletResponse.setHeader("Access-Control-Allow-Methods", "*")
        /* 重新预检验跨域的缓存时间 (s) */
//		httpServletResponse.setHeader("Access-Control-Max-Age", "4200");  
        /* 允许跨域的请求头 */httpServletResponse.setHeader("Access-Control-Allow-Headers", "*")
        /* 是否携带cookie */httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true")

        logger.info("GlobalFilter: ${httpServletRequest.requestURL}")

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
            if (!TokenUtil.valid(token)) {
                response.getWriter().write(gson.toJson(
                        Response<Any?>(CodeMessage.TOKEN_ILLEGAL.code, CodeMessage.TOKEN_ILLEGAL.message, null)))
                return
            }
            chain.doFilter(request, response)
        }
    }
}