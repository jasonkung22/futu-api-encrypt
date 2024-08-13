package cn.futuai.open.encrypt.spring.boot.exception.handler;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * api异常请求处理器
 * @author Jason Kung
 * @date 2024/6/7 14:16
 */
@FunctionalInterface
public interface ApiExceptionRequestHandler {

    /**
     * Handle the request when api valid.
     * @param request  Servlet request
     * @param response Servlet response
     * @param e        the api valid exception
     * @throws Exception users may throw out the BlockException or other error occurs
     */
    void handleRequest(HttpServletRequest request, HttpServletResponse response, Throwable e) throws Exception;
}
