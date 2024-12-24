package cn.futuai.open.encrypt.spring.boot.exception;

import cn.futuai.open.encrypt.core.exception.ApiBaseException;
import cn.futuai.open.encrypt.core.log.ApiEncryptLogger;
import cn.futuai.open.encrypt.spring.boot.callback.SpringBootApiExceptionCallbackManager;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 网关api异常处理器
 * @author Jason Kung
 * @date 2024/6/7 15:21
 */
public class ApiExceptionHandler {

    private final ApiEncryptLogger apiEncryptLogger;

    public ApiExceptionHandler(ApiEncryptLogger apiEncryptLogger) {
        this.apiEncryptLogger = apiEncryptLogger;
    }

    public void apiExceptionHandler(HttpServletRequest request, HttpServletResponse response,
            ApiBaseException e) throws IOException {
        apiEncryptLogger.logException(e);
        SpringBootApiExceptionCallbackManager.getApiExceptionHandler().handleRequest(request, response, e);
    }
}
