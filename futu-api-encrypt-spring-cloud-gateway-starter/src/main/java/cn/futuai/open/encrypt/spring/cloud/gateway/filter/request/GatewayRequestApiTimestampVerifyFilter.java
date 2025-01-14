package cn.futuai.open.encrypt.spring.cloud.gateway.filter.request;

import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiTimestampException;
import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.futuai.open.encrypt.spring.cloud.gateway.config.property.GatewayApiEncryptProperties;
import cn.futuai.open.encrypt.spring.cloud.gateway.filter.AbstractGatewayFilter;
import cn.hutool.core.util.StrUtil;
import javax.annotation.Resource;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求时间戳校验
 * @author Jason Kung
 * @date 2024/6/7 11:25
 */
public class GatewayRequestApiTimestampVerifyFilter extends AbstractGatewayFilter {

    @Resource
    private GatewayApiEncryptProperties gatewayApiEncryptProperty;

    @Override
    protected GatewayApiEncryptProperties getGatewayApiEncryptProperty() {
        return gatewayApiEncryptProperty;
    }

    @Override
    protected Mono<Void> doFilterInternal(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestUri = exchange.getRequest().getURI().getPath();
        String timestampStr = exchange.getAttribute(ApiEncryptConstant.TIMES_TAMP);

        if (StrUtil.isBlankIfStr(timestampStr) || !StrUtil.isNumeric(timestampStr)) {
            throw new ApiTimestampException(requestUri, timestampStr);
        }
        long timestamp = Long.parseLong(timestampStr);
        if (!ApiEncryptUtil.isTimestampValid(timestamp,
                gatewayApiEncryptProperty.getTimestamp().getTimestampValidSecond())) {
            throw new ApiTimestampException(requestUri, timestampStr);
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
