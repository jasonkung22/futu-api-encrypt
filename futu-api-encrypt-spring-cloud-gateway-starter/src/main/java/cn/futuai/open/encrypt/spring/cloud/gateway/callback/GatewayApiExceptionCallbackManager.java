package cn.futuai.open.encrypt.spring.cloud.gateway.callback;

import cn.futuai.open.encrypt.core.callback.ApiExceptionCallbackManager;
import cn.futuai.open.encrypt.spring.cloud.gateway.exception.handler.GatewayApiExceptionRequestHandler;
import cn.futuai.open.encrypt.spring.cloud.gateway.exception.handler.impl.DefaultGatewayApiExceptionRequestHandler;
import cn.hutool.core.lang.Assert;

/**
 * 网关api异常回调管理器
 * @author Jason Kung
 * @date 2024/6/7 14:15
 */
public final class GatewayApiExceptionCallbackManager implements ApiExceptionCallbackManager {

    /**
     * ApiRequestExceptionHandler: (serverExchange, exception) -> response
     */
    private static volatile GatewayApiExceptionRequestHandler apiExceptionHandler = new DefaultGatewayApiExceptionRequestHandler();

    public static /*@NonNull*/ GatewayApiExceptionRequestHandler getApiExceptionHandler() {
        return apiExceptionHandler;
    }

    public static void resetApiExceptionHandler() {
        GatewayApiExceptionCallbackManager.apiExceptionHandler = new DefaultGatewayApiExceptionRequestHandler();
    }

    public static void setApiExceptionHandler(GatewayApiExceptionRequestHandler apiExceptionHandler) {
        Assert.notNull(apiExceptionHandler, "apiInvalidHandler cannot be null");
        GatewayApiExceptionCallbackManager.apiExceptionHandler = apiExceptionHandler;
    }

    private GatewayApiExceptionCallbackManager() {
    }
}
