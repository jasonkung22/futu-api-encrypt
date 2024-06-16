package cn.futuai.example.config;

import cn.futuai.open.encrypt.callback.ApiValidExceptionRequestHandler;
import cn.futuai.open.encrypt.callback.GatewayApiInvalidCallbackManager;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * api加密配置
 * @author Jason
 * @date 2022/7/20 21:19
 */
@Configuration
public class ApiEncryptConfiguration {

    public ApiEncryptConfiguration() {
        // 网关回调管理器
        GatewayApiInvalidCallbackManager.setApiInvalidHandler(new ApiValidExceptionRequestHandler() {
            /**
             * 网关API校验失败，就会调用此回调
             * @param serverWebExchange serverWebExchange
             * @param throwable 异常
             * @return mono
             */
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange,
                    Throwable throwable) {
                Response<Void> error = Response.create(ResponseTypeEnum.PARAM_VALID_ERROR);
                String errJson = JSONUtil.toJsonStr(error);
                return ServerResponse.ok().body(Mono.just(errJson), String.class);
            }
        });
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response<T> {

        /**
         * 响应码
         */
        private String code;
        /**
         * 响应消息
         */
        private String message;
        /**
         * 响应数据
         */
        private T data;

        /**
         * 根据响应类型创建响应，不带响应数据
         * @param responseType 响应类型，为空时，返回SYSTEM_ERROR
         * @return 响应
         */
        public static Response<Void> create(ResponseTypeEnum responseType) {
            if (responseType == null) {
                return ResponseTypeEnum.SYSTEM_ERROR.getVoidResponse();
            }

            return new Response<>(responseType.getCode(), responseType.getMessage(), null);
        }
    }

    @Getter
    public enum ResponseTypeEnum {

        /**
         * 成功
         */
        SUCCESS("00000", "成功"),
        /**
         * 系统错误，请重试
         */
        SYSTEM_ERROR("00001", "系统错误，请重试", true),
        /**
         * 参数校验错误
         */
        PARAM_VALID_ERROR("00002", "参数校验错误", true),
        ;


        private final String code;
        private final String message;
        private final boolean printException;
        private final Response<Void> voidResponse;


        ResponseTypeEnum(String code, String message, boolean printException) {
            this.code = code;
            this.message = message;
            this.printException = printException;
            this.voidResponse = Response.create(this);
        }

        ResponseTypeEnum(String code, String message) {
            this.code = code;
            this.message = message;
            this.printException = false;
            this.voidResponse = Response.create(this);
        }
    }
}
