package cn.futuai.open.encrypt.spring.boot.callback;

import cn.futuai.open.encrypt.core.callback.ApiExceptionCallbackManager;
import cn.futuai.open.encrypt.spring.boot.exception.handler.SpringBootApiExceptionRequestHandler;
import cn.futuai.open.encrypt.spring.boot.exception.handler.impl.DefaultSpringBootApiExceptionRequestHandler;
import cn.hutool.core.lang.Assert;

/**
 * 网关加密回调管理器
 * @author Jason Kung
 * @date 2024/6/7 14:15
 */
public final class SpringBootApiExceptionCallbackManager implements ApiExceptionCallbackManager {

    /**
     * ApiRequestExceptionHandler: (serverExchange, exception) -> response
     */
    private static volatile SpringBootApiExceptionRequestHandler apiExceptionHandler = new DefaultSpringBootApiExceptionRequestHandler();

    public static /*@NonNull*/ SpringBootApiExceptionRequestHandler getApiExceptionHandler() {
        return apiExceptionHandler;
    }

    public static void resetApiExceptionHandler() {
        SpringBootApiExceptionCallbackManager.apiExceptionHandler = new DefaultSpringBootApiExceptionRequestHandler();
    }

    public static void setApiExceptionHandler(SpringBootApiExceptionRequestHandler apiExceptionHandler) {
        Assert.notNull(apiExceptionHandler, "apiExceptionHandler cannot be null");
        SpringBootApiExceptionCallbackManager.apiExceptionHandler = apiExceptionHandler;
    }

    private SpringBootApiExceptionCallbackManager() {
    }
}
