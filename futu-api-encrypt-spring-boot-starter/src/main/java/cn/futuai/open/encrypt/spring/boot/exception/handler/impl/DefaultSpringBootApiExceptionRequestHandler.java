package cn.futuai.open.encrypt.spring.boot.exception.handler.impl;


import cn.futuai.open.encrypt.spring.boot.exception.handler.SpringBootApiExceptionRequestHandler;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

/**
 * 默认spring boot api请求异常处理器
 * @author Jason Kung
 * @date 2024/6/7 15:05
 */
public class DefaultSpringBootApiExceptionRequestHandler implements SpringBootApiExceptionRequestHandler {

    private static final String DEFAULT_EXCEPTION_MSG = "invalid param: ";

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response, Throwable e)
            throws IOException {
        // Return 403 (Request Forbidden) by default.
        response.setStatus(HttpStatus.FORBIDDEN.value());

        PrintWriter out = response.getWriter();
        out.print(DEFAULT_EXCEPTION_MSG + e.getClass().getSimpleName());
        out.flush();
        out.close();
    }
}
