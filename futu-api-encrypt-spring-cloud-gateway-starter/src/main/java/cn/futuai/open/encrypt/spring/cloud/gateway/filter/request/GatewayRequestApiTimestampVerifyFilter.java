package cn.futuai.open.encrypt.spring.cloud.gateway.filter.request;

import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiTimestampException;
import cn.futuai.open.encrypt.core.property.TimestampVerify;
import cn.futuai.open.encrypt.core.util.ApiChecker;
import cn.futuai.open.encrypt.spring.cloud.gateway.config.property.GatewayApiEncryptProperties;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import java.util.Date;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求时间戳校验
 * @author Jason Kung
 * @date 2024/6/7 11:25
 */
@Slf4j
public class GatewayRequestApiTimestampVerifyFilter implements GlobalFilter, Ordered {

    @Resource
    private GatewayApiEncryptProperties gatewayApiEncryptProperty;

    @Override
    @SneakyThrows
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestUri = request.getURI().getPath();

        if (ApiChecker.isPass(requestUri, gatewayApiEncryptProperty.getEnabled(),
                gatewayApiEncryptProperty.getCheckModel())) {
            return chain.filter(exchange);
        }

        TimestampVerify timestampVerify = gatewayApiEncryptProperty.getTimestamp();
        if (!timestampVerify.getEnabled()) {
            return chain.filter(exchange);
        }

        String timestampStr = exchange.getAttribute(ApiEncryptConstant.TIMES_TAMP);
        if (StrUtil.isBlankIfStr(timestampStr) || !StrUtil.isNumeric(timestampStr)) {
            log.error("请求参数时间戳格式不正确,requestUri:{}, timestamp:{}", requestUri, timestampStr);
            throw new ApiTimestampException();
        }
        long timestamp = Long.parseLong(timestampStr);
        if (DateUtil.between(new Date(timestamp), new Date(), DateUnit.SECOND)
                > timestampVerify.getTimestampValidSecond()) {
            log.error("请求参数时间戳校验失败,requestUri:{}, timestamp:{}", requestUri, timestamp);
            throw new ApiTimestampException();
        }

        return chain.filter(exchange);
    }


    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
