/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.futuai.open.encrypt.exception.handler;

import cn.futuai.open.encrypt.callback.GatewayApiInvalidCallbackManager;
import cn.futuai.open.encrypt.exception.ApiValidException;
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
public class GatewayApiValidExceptionHandler implements WebExceptionHandler {

    private final List<ViewResolver> viewResolvers;
    private final List<HttpMessageWriter<?>> messageWriters;

    public GatewayApiValidExceptionHandler(List<ViewResolver> viewResolvers,
            ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolvers;
        this.messageWriters = serverCodecConfigurer.getWriters();
    }

    private Mono<Void> writeResponse(ServerResponse response, ServerWebExchange exchange) {
        return response.writeTo(exchange, contextSupplier.get());
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        // This exception handler only handles rejection by Sentinel.
        if (!(ex instanceof ApiValidException)) {
            return Mono.error(ex);
        }
        return handleApiInvalidRequest(exchange, ex)
                .flatMap(response -> writeResponse(response, exchange));
    }

    private Mono<ServerResponse> handleApiInvalidRequest(ServerWebExchange exchange, Throwable throwable) {
        return GatewayApiInvalidCallbackManager.getApiInvalidHandler().handleRequest(exchange, throwable);
    }

    private final Supplier<Context> contextSupplier = () -> new ServerResponse.Context() {
        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            return GatewayApiValidExceptionHandler.this.messageWriters;
        }

        @Override
        public List<ViewResolver> viewResolvers() {
            return GatewayApiValidExceptionHandler.this.viewResolvers;
        }
    };
}
