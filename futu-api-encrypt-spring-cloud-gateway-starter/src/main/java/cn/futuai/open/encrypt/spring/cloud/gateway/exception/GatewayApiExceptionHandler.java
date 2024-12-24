package cn.futuai.open.encrypt.spring.cloud.gateway.exception;

import cn.futuai.open.encrypt.core.exception.ApiBaseException;
import cn.futuai.open.encrypt.core.log.ApiEncryptLogger;
import cn.futuai.open.encrypt.spring.cloud.gateway.callback.GatewayApiExceptionCallbackManager;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.ServerResponse.Context;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * 网关api有效性异常处理器
 * @author Jason Kung
 * @date 2024/6/7 15:21
 */
@SuppressWarnings("NullableProblems")
public class GatewayApiExceptionHandler implements WebExceptionHandler {

    private final List<ViewResolver> viewResolvers;
    private final List<HttpMessageWriter<?>> messageWriters;

    private final ApiEncryptLogger apiEncryptLogger;

    public GatewayApiExceptionHandler(List<ViewResolver> viewResolvers,
            ServerCodecConfigurer serverCodecConfigurer, ApiEncryptLogger apiEncryptLogger) {
        this.viewResolvers = viewResolvers;
        this.messageWriters = serverCodecConfigurer.getWriters();
        this.apiEncryptLogger = apiEncryptLogger;
    }

    private Mono<Void> writeResponse(ServerResponse response, ServerWebExchange exchange) {
        return response.writeTo(exchange, contextSupplier.get());
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        if (!(ex instanceof ApiBaseException)) {
            return Mono.error(ex);
        }

        logApiException(ex);

        return handleApiExceptionRequest(exchange, ex)
                .flatMap(response -> writeResponse(response, exchange));
    }

    private void logApiException(Throwable throwable) {
        apiEncryptLogger.logException(throwable);
    }

    private Mono<ServerResponse> handleApiExceptionRequest(ServerWebExchange exchange, Throwable throwable) {
        return GatewayApiExceptionCallbackManager.getApiExceptionHandler().handleRequest(exchange, throwable);
    }

    private final Supplier<Context> contextSupplier = () -> new ServerResponse.Context() {
        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            return GatewayApiExceptionHandler.this.messageWriters;
        }

        @Override
        public List<ViewResolver> viewResolvers() {
            return GatewayApiExceptionHandler.this.viewResolvers;
        }
    };
}
