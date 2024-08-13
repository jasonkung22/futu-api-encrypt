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
package cn.futuai.open.encrypt.spring.cloud.gateway.exception.handler.impl;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import cn.futuai.open.encrypt.spring.cloud.gateway.exception.handler.GatewayApiExceptionRequestHandler;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 默认api请求异常处理器
 * @author Jason Kung
 * @date 2024/6/7 15:05
 */
public class DefaultGatewayApiExceptionRequestHandler implements GatewayApiExceptionRequestHandler {

    private static final String DEFAULT_EXCEPTION_MSG = "invalid param: ";

    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable ex) {
        if (acceptsHtml(exchange)) {
            return htmlErrorResponse(ex);
        }
        return ServerResponse.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(buildErrorResult(ex)));
    }

    private Mono<ServerResponse> htmlErrorResponse(Throwable ex) {
        return ServerResponse.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue(DEFAULT_EXCEPTION_MSG + ex.getClass().getSimpleName());
    }

    private ErrorResult buildErrorResult(Throwable ex) {
        return new ErrorResult(HttpStatus.FORBIDDEN.value(),
                DEFAULT_EXCEPTION_MSG + ex.getClass().getSimpleName());
    }

    private boolean acceptsHtml(ServerWebExchange exchange) {
        try {
            List<MediaType> acceptedMediaTypes = exchange.getRequest().getHeaders().getAccept();
            acceptedMediaTypes.remove(MediaType.ALL);
            MediaType.sortBySpecificityAndQuality(acceptedMediaTypes);
            return acceptedMediaTypes.stream()
                    .anyMatch(MediaType.TEXT_HTML::isCompatibleWith);
        } catch (InvalidMediaTypeException ex) {
            return false;
        }
    }

    private static class ErrorResult {

        private final int code;
        private final String message;

        ErrorResult(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
