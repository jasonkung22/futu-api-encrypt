package cn.futuai.open.encrypt.spring.cloud.gateway.filter.request;

import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiDecryptException;
import cn.futuai.open.encrypt.core.property.RequestDecrypt;
import cn.futuai.open.encrypt.core.util.ApiChecker;
import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.futuai.open.encrypt.spring.cloud.gateway.config.property.GatewayApiEncryptProperties;
import cn.futuai.open.encrypt.spring.cloud.gateway.filter.AbstractGatewayFilter;
import cn.hutool.core.util.StrUtil;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import javax.annotation.Resource;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求解密过滤器
 * @author Jason Kung
 * @date 2023/10/10 17:12
 */
public class GatewayRequestApiDecryptFilter extends AbstractGatewayFilter {

    @Resource
    private ModifyRequestBodyGatewayFilterFactory encryptFilterFactory;
    @Resource
    private GatewayApiEncryptProperties gatewayApiEncryptProperty;

    @Override
    protected GatewayApiEncryptProperties getGatewayApiEncryptProperty() {
        return gatewayApiEncryptProperty;
    }

    @Override
    protected Mono<Void> doFilterInternal(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestUri = request.getURI().getPath();

        // 检查是否在解密接口列表中
        RequestDecrypt requestDecrypt = gatewayApiEncryptProperty.getRequestDecrypt();
        if (ApiChecker.isPass(requestUri, requestDecrypt.getEnabled(), requestDecrypt.getCheckModel())) {
            return chain.filter(exchange);
        }
        String queryString = exchange.getAttribute(ApiEncryptConstant.ORG_QUERY_STRING);
        String aesKey = exchange.getAttribute(ApiEncryptConstant.AES_KEY);
        try {
            String decryptRequestParam = decryptRequestParam(queryString, aesKey);
            updateRequestParam(exchange, decryptRequestParam);
        } catch (Exception e) {
            throw new ApiDecryptException(requestUri, queryString, aesKey, e);
        }

        String orgBody = exchange.getAttribute(ApiEncryptConstant.ORG_BODY);
        if (StrUtil.isBlank(orgBody)) {
            return chain.filter(exchange);
        }

        return encryptFilterFactory
                .apply(new ModifyRequestBodyGatewayFilterFactory.Config().setRewriteFunction(byte[].class,
                        byte[].class, new RequestDecryptRewriter()))
                .filter(exchange, chain);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 30;
    }

    static class RequestDecryptRewriter implements RewriteFunction<byte[], byte[]> {

        @Override
        public Publisher<byte[]> apply(ServerWebExchange exchange, byte[] bytes) {
            return Mono.just(decrypt(exchange, bytes));
        }

        public byte[] decrypt(ServerWebExchange exchange, byte[] bytes) {
            String aesKey = exchange.getAttribute(ApiEncryptConstant.AES_KEY);
            if (bytes == null || bytes.length == 0 || StrUtil.isBlank(aesKey)) {
                return bytes;
            }
            String decryptResult;
            String requestUri = exchange.getRequest().getURI().getPath();
            String text = new String(bytes);
            try {
                decryptResult = ApiEncryptUtil.aesDecrypt(text, aesKey);
            } catch (Exception e) {
                throw new ApiDecryptException(requestUri, text, aesKey, e);
            }
            return decryptResult.getBytes(StandardCharsets.UTF_8);
        }
    }

    private String decryptRequestParam(String queryString, String aesKey) {
        if (StrUtil.isNotBlank(queryString) && queryString.contains(gatewayApiEncryptProperty.getEncryptParamKey())) {
            String[] split = queryString.split("=");
            String paramValue = split[1];
            //解密请求参数
            return ApiEncryptUtil.aesDecrypt(paramValue, aesKey);
        }
        return null;
    }

    private void updateRequestParam(ServerWebExchange exchange, String param) throws Exception {
        if (StrUtil.isBlank(param)) {
            return;
        }
        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        Field targetQuery = uri.getClass().getDeclaredField("query");
        targetQuery.setAccessible(true);
        targetQuery.set(uri, param);
    }
}
