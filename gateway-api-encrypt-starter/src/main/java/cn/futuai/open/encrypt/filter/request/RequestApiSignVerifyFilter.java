package cn.futuai.open.encrypt.filter.request;

import cn.futuai.open.encrypt.config.property.GatewayApiEncryptProperty;
import cn.futuai.open.encrypt.config.property.GatewayApiEncryptProperty.SignVerify;
import cn.futuai.open.encrypt.exception.ApiSignValidException;
import cn.futuai.open.encrypt.util.ApiEncryptUtil;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 验证签名
 * @author Jason Kung
 * @date 2023/11/07 13:37
 */
@Slf4j
public class RequestApiSignVerifyFilter implements GlobalFilter, Ordered {

    @Resource
    private GatewayApiEncryptProperty gatewayApiEncryptProperty;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        SignVerify signVerify = gatewayApiEncryptProperty.getSign();
        if (!signVerify.getEnable()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();

        if (RequestApiFilter.isPass(request, gatewayApiEncryptProperty.getCheckModel())) {
            return chain.filter(exchange);
        }

        String sign = exchange.getAttribute(RequestApiFilter.SIGN);
        String aesKey = exchange.getAttribute(RequestApiFilter.AES_KEY);
        String timestamp = exchange.getAttribute(RequestApiFilter.TIMES_TAMP);
        String orgQueryString = exchange.getAttribute(RequestApiFilter.ORG_QUERY_STRING);
        String orgBody = exchange.getAttribute(RequestApiFilter.ORG_BODY);

        if (!ApiEncryptUtil.verifySign(timestamp, aesKey, orgQueryString, orgBody, sign)) {
            log.error("请求参数验签失败,timestamp:{}, aesKey:{}, orgQueryString:{}, orgBody:{}, sign:{}", timestamp,
                    aesKey, orgQueryString, orgBody, sign);
            return Mono.error(new ApiSignValidException());
        }
        return chain.filter(exchange);
    }


    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}
