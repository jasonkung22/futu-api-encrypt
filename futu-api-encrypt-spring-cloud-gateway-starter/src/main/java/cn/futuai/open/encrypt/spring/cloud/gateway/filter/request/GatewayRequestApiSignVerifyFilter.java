package cn.futuai.open.encrypt.spring.cloud.gateway.filter.request;

import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiSignException;
import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.futuai.open.encrypt.spring.cloud.gateway.config.property.GatewayApiEncryptProperties;
import cn.futuai.open.encrypt.spring.cloud.gateway.filter.AbstractGatewayFilter;
import javax.annotation.Resource;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 验证签名
 * @author Jason Kung
 * @date 2023/11/07 13:37
 */
public class GatewayRequestApiSignVerifyFilter extends AbstractGatewayFilter {

    @Resource
    private GatewayApiEncryptProperties gatewayApiEncryptProperty;

    @Override
    protected GatewayApiEncryptProperties getGatewayApiEncryptProperty() {
        return gatewayApiEncryptProperty;
    }

    @Override
    protected Mono<Void> doFilterInternal(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestUri = exchange.getRequest().getURI().getPath();
        String sign = exchange.getAttribute(ApiEncryptConstant.SIGN);
        String aesKey = exchange.getAttribute(ApiEncryptConstant.AES_KEY);
        String timestamp = exchange.getAttribute(ApiEncryptConstant.TIMES_TAMP);
        String orgQueryString = exchange.getAttribute(ApiEncryptConstant.ORG_QUERY_STRING);
        String orgBody = exchange.getAttribute(ApiEncryptConstant.ORG_BODY);

        if (!ApiEncryptUtil.verifySign(timestamp, aesKey, orgQueryString, orgBody, sign)) {
            throw new ApiSignException(requestUri, timestamp, aesKey, orgQueryString, orgBody, sign);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}
