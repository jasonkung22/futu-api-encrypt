package cn.futuai.open.encrypt.spring.boot.exception.handler.impl;


import cn.futuai.open.encrypt.core.exception.ApiDecryptException;
import cn.futuai.open.encrypt.core.exception.ApiEncryptException;
import cn.futuai.open.encrypt.core.exception.ApiSignException;
import cn.futuai.open.encrypt.core.exception.ApiTimestampException;
import cn.futuai.open.encrypt.spring.boot.exception.handler.SpringBootApiExceptionRequestHandler;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 * 默认spring boot api请求异常处理器
 * @author Jason Kung
 * @date 2024/6/7 15:05
 */
public class DefaultSpringBootApiExceptionRequestHandler implements SpringBootApiExceptionRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultSpringBootApiExceptionRequestHandler.class);

    private static final String DEFAULT_EXCEPTION_MSG = "invalid param: ";

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response, Throwable e)
            throws IOException {
        logException(e);

        // Return 403 (Request Forbidden) by default.
        response.setStatus(HttpStatus.FORBIDDEN.value());

        PrintWriter out = response.getWriter();
        out.print(DEFAULT_EXCEPTION_MSG + e.getClass().getSimpleName());
        out.flush();
        out.close();
    }

    public void logException(Throwable ex) {
        if (ex instanceof ApiTimestampException) {
            ApiTimestampException apiTimestampException = (ApiTimestampException) ex;
            log.error("api timestamp exception, requestUri:{}, timestamp:{}", apiTimestampException.getRequestUri(),
                    apiTimestampException.getTimestamp(), ex);
        } else if (ex instanceof ApiSignException) {
            ApiSignException apiSignException = (ApiSignException) ex;
            log.error("api sign exception, requestUri:{}, timestamp:{}, aesKey:{}, orgQueryString:{}, orgBody:{}",
                    apiSignException.getRequestUri(),
                    apiSignException.getTimestamp(), apiSignException.getAesKey(), apiSignException.getOrgQueryString(),
                    apiSignException.getOrgBody(), ex);
        } else if (ex instanceof ApiDecryptException) {
            ApiDecryptException apiDecryptException = (ApiDecryptException) ex;
            log.error("api decrypt exception, requestUri:{}, param:{}, aesKey:{}", apiDecryptException.getRequestUri(),
                    apiDecryptException.getParam(), apiDecryptException.getAesKey(), ex);
        } else if (ex instanceof ApiEncryptException) {
            ApiEncryptException apiEncryptException = (ApiEncryptException) ex;
            log.error("api encrypt exception, requestUri:{}, param:{}, aesKey:{}", apiEncryptException.getRequestUri(),
                    apiEncryptException.getParam(), apiEncryptException.getAesKey(), ex);
        }
    }
}
