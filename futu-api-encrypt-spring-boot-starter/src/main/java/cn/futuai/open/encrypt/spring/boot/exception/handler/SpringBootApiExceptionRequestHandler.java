package cn.futuai.open.encrypt.spring.boot.exception.handler;


import cn.futuai.open.encrypt.core.exception.handler.ApiExceptionRequestHandler;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * spring boot api异常请求处理器
 * @author Jason Kung
 * @date 2024/6/7 14:16
 */
@FunctionalInterface
public interface SpringBootApiExceptionRequestHandler extends ApiExceptionRequestHandler {

    /**
     * Handle the request when api valid.
     * @param request  Servlet request
     * @param response Servlet response
     * @param e        the api valid exception
     * @throws IOException users may throw out the ApiException or other error occurs
     */
    void handleRequest(HttpServletRequest request, HttpServletResponse response, Throwable e) throws IOException;

}
