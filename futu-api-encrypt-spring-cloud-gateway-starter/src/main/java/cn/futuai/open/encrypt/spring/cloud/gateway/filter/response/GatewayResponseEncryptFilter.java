package cn.futuai.open.encrypt.spring.cloud.gateway.filter.response;

import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiEncryptException;
import cn.futuai.open.encrypt.core.property.ResponseEncrypt;
import cn.futuai.open.encrypt.core.util.ApiChecker;
import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.futuai.open.encrypt.spring.cloud.gateway.config.property.GatewayApiEncryptProperties;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import java.nio.charset.StandardCharsets;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 返回加密过滤器
 * @author Jason Kung
 * @date 2023/10/10 17:12
 */
@Slf4j
public class GatewayResponseEncryptFilter implements GlobalFilter, Ordered {


    @Resource
    private ModifyResponseBodyGatewayFilterFactory encryptFilterFactory;

    @Resource
    private GatewayApiEncryptProperties gatewayApiEncryptProperty;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestUri = request.getURI().getPath();

        if (ApiChecker.isPass(requestUri, gatewayApiEncryptProperty.getEnabled(),
                gatewayApiEncryptProperty.getCheckModel())) {
            return chain.filter(exchange);
        }

        ResponseEncrypt responseEncrypt = gatewayApiEncryptProperty.getResponseEncrypt();

        if (ApiChecker.isPass(requestUri, responseEncrypt.getEnabled(), responseEncrypt.getCheckModel())) {
            return chain.filter(exchange);
        }

        return encryptFilterFactory.apply(
                new ModifyResponseBodyGatewayFilterFactory.Config().setRewriteFunction(byte[].class,
                        byte[].class, new ResponseEncryptRewriter())).filter(exchange, chain);
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }

    static class ResponseEncryptRewriter implements RewriteFunction<byte[], byte[]> {

        @SneakyThrows
        @Override
        public Publisher<byte[]> apply(ServerWebExchange exchange, byte[] bytes) {
            if (!MediaType.APPLICATION_JSON.equals(exchange.getResponse().getHeaders().getContentType())) {
                return Mono.just(bytes);
            }
            return Mono.just(encrypt(exchange, bytes));
        }

        @SneakyThrows
        private byte[] encrypt(ServerWebExchange exchange, byte[] jsonBytes) {
            String aesKey = exchange.getAttribute(ApiEncryptConstant.AES_KEY);
            if (ArrayUtil.isEmpty(jsonBytes) || StrUtil.isBlank(aesKey)) {
                return jsonBytes;
            }

            String encryptResult;
            String requestUri = exchange.getRequest().getURI().getPath();
            try {
                String json = new String(jsonBytes);
                encryptResult = ApiEncryptUtil.aesEncrypt(json, aesKey);
            } catch (Exception e) {
                log.error("响应结果加密异常,requestUri:{}，json:{}, aesKey:{}", requestUri,
                        jsonBytes, aesKey, e);
                throw new ApiEncryptException();
            }
            return encryptResult.getBytes(StandardCharsets.UTF_8);

        }
    }

}