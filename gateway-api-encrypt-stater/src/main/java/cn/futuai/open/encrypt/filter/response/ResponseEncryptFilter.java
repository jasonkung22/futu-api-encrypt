package cn.futuai.open.encrypt.filter.response;

import cn.futuai.open.encrypt.config.property.GatewayApiEncryptProperty;
import cn.futuai.open.encrypt.config.property.GatewayApiEncryptProperty.CheckModel;
import cn.futuai.open.encrypt.filter.request.RequestApiFilter;
import cn.futuai.open.encrypt.util.ApiEncryptUtil;
import cn.hutool.core.util.StrUtil;
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
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 返回加密过滤器
 * @author Jason
 * @date 2023/10/10 17:12
 */
@Slf4j
public class ResponseEncryptFilter implements GlobalFilter, Ordered {

    @Resource
    private ModifyResponseBodyGatewayFilterFactory encryptFilterFactory;

    @Resource
    private GatewayApiEncryptProperty gatewayApiEncryptProperty;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!gatewayApiEncryptProperty.getEnableResponseEncrypt()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String url = request.getURI().getPath();

        boolean isNeedEncrypt = CheckModel.WHITE_LIST.equals(gatewayApiEncryptProperty.getResponseEncryptCheckModel())
                && !RequestApiFilter.isMatchUrl(url, gatewayApiEncryptProperty.getResponseEncryptWhiteList());

        if (CheckModel.BLACK_LIST.equals(gatewayApiEncryptProperty.getResponseEncryptCheckModel())
                && RequestApiFilter.isMatchUrl(url, gatewayApiEncryptProperty.getResponseEncryptBlackList())) {
            isNeedEncrypt = true;
        }

        if (!isNeedEncrypt) {
            return chain.filter(exchange);
        }

        return encryptFilterFactory.apply(
                new ModifyResponseBodyGatewayFilterFactory.Config().setRewriteFunction(String.class,
                        String.class, new ResponseEncryptRewriter())).filter(exchange, chain);
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }

    static class ResponseEncryptRewriter implements RewriteFunction<String, String> {

        @SneakyThrows
        @Override
        public Publisher<String> apply(ServerWebExchange exchange, String json) {
            return Mono.just(encrypt(json, exchange.getAttribute(RequestApiFilter.AES_KEY)));
        }

        private String encrypt(String json, String aesKey) {
            if (StrUtil.isNotBlank(json) || StrUtil.isNotBlank(aesKey)) {
                String encryptResult = "";
                try {
                    encryptResult = ApiEncryptUtil.aesEncrypt(json, aesKey);
                } catch (Exception e) {
                    log.error("响应结果加密异常，json:{}, aesKey:{}", json, aesKey, e);
                }
                return encryptResult;
            }
            return json;
        }
    }

}