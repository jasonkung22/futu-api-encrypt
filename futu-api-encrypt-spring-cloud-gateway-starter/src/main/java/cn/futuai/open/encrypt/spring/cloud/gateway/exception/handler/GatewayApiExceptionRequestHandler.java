package cn.futuai.open.encrypt.spring.cloud.gateway.exception.handler;

import cn.futuai.open.encrypt.core.exception.handler.ApiExceptionRequestHandler;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * api有效性异常请求处理器
 * @author Jason Kung
 * @date 2024/6/7 14:16
 */
@FunctionalInterface
public interface GatewayApiExceptionRequestHandler extends ApiExceptionRequestHandler {

    /**
     * Handle the api request.
     * @param exchange server exchange object
     * @param t        api exception
     * @return server response to return
     */
    Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t);
}
