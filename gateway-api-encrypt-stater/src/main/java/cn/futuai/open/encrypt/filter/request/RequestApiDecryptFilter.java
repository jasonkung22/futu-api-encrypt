package cn.futuai.open.encrypt.filter.request;

import cn.futuai.open.encrypt.config.property.GatewayApiEncryptProperty;
import cn.futuai.open.encrypt.config.property.GatewayApiEncryptProperty.CheckModel;
import cn.futuai.open.encrypt.exception.ApiDecryptException;
import cn.futuai.open.encrypt.util.ApiEncryptUtil;
import cn.hutool.core.util.StrUtil;
import java.lang.reflect.Field;
import java.net.URI;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求解密过滤器
 * @author Jason
 * @date 2023/10/10 17:12
 */
@Slf4j
public class RequestApiDecryptFilter implements GlobalFilter, Ordered {

    @Resource
    private ModifyRequestBodyGatewayFilterFactory encryptFilterFactory;
    @Resource
    private GatewayApiEncryptProperty gatewayApiEncryptProperty;

    private static final String ENCRYPT_QUERY_STRING_KEY = "ciphertext";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!gatewayApiEncryptProperty.getEnableDecrypt()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String url = request.getURI().getPath();

        boolean isNeedDecrypt = CheckModel.WHITE_LIST.equals(gatewayApiEncryptProperty.getRequestDecryptCheckModel())
                && !RequestApiFilter.isMatchUrl(url, gatewayApiEncryptProperty.getRequestDecryptWhiteList());

        if (CheckModel.BLACK_LIST.equals(gatewayApiEncryptProperty.getRequestDecryptCheckModel())
                && RequestApiFilter.isMatchUrl(url, gatewayApiEncryptProperty.getRequestDecryptBlackList())) {
            isNeedDecrypt = true;
        }

        if (!isNeedDecrypt) {
            return chain.filter(exchange);
        }

        try {
            String decryptRequestParam = decryptRequestParam(exchange);
            updateRequestParam(exchange, decryptRequestParam);
        } catch (Exception e) {
            return Mono.error(new ApiDecryptException());
        }

        return encryptFilterFactory
                .apply(new ModifyRequestBodyGatewayFilterFactory.Config().setRewriteFunction(String.class,
                        String.class, new RequestDecryptRewriter()))
                .filter(exchange, chain);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 30;
    }

    static class RequestDecryptRewriter implements RewriteFunction<String, String> {

        @Override
        public Publisher<String> apply(ServerWebExchange exchange, String text) {
            return Mono.just(decrypt(text, exchange.getAttribute(RequestApiFilter.AES_KEY)));
        }

        public String decrypt(String text, String aesKey) {
            if (StrUtil.isBlank(text) || StrUtil.isBlank(aesKey)) {
                return "";
            }
            return ApiEncryptUtil.aesDecrypt(text, aesKey);
        }
    }

    @SneakyThrows
    private String decryptRequestParam(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        String queryString = uri.getQuery();
        try {
            if (StrUtil.isNotBlank(queryString) && queryString.contains(ENCRYPT_QUERY_STRING_KEY)) {
                String[] split = queryString.split("=");
                String paramValue = split[1];
                //解密请求参数
                return ApiEncryptUtil.aesDecrypt(paramValue, exchange.getAttribute(RequestApiFilter.AES_KEY));
            }
        } catch (Exception e) {
            log.error("解密请求参数异常, queryString:{}", queryString, e);
            throw new ApiDecryptException();
        }
        return null;
    }

    /**
     * 修改前端传的参数
     */
    @SneakyThrows
    private void updateRequestParam(ServerWebExchange exchange, String param) {
        if (StrUtil.isBlank(param)) {
            return;
        }
        try {
            ServerHttpRequest request = exchange.getRequest();
            URI uri = request.getURI();
            Field targetQuery = uri.getClass().getDeclaredField("query");
            targetQuery.setAccessible(true);
            targetQuery.set(uri, param);
        } catch (Exception e) {
            log.error("修改请求参数异常, param:{}", param, e);
            throw new ApiDecryptException();
        }
    }

}
