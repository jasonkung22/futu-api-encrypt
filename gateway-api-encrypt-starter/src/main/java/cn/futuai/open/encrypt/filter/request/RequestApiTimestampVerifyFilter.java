package cn.futuai.open.encrypt.filter.request;

import cn.futuai.open.encrypt.config.property.GatewayApiEncryptProperty;
import cn.futuai.open.encrypt.config.property.GatewayApiEncryptProperty.TimestampVerify;
import cn.futuai.open.encrypt.exception.ApiTimestampValidException;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import java.util.Date;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求时间戳校验
 * @author gyf
 * @date 2024/6/7 11:25
 */
@Slf4j
public class RequestApiTimestampVerifyFilter implements GlobalFilter, Ordered {

    @Resource
    private GatewayApiEncryptProperty gatewayApiEncryptProperty;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        TimestampVerify timestampVerify = gatewayApiEncryptProperty.getTimestamp();
        if (!timestampVerify.getEnable()) {
            return chain.filter(exchange);
        }

        if (RequestApiFilter.isPass(exchange.getRequest(), gatewayApiEncryptProperty.getCheckModel())) {
            return chain.filter(exchange);
        }

        String timestampStr = exchange.getAttribute(RequestApiFilter.TIMES_TAMP);
        try {
            if (StrUtil.isBlankIfStr(timestampStr) || !StrUtil.isNumeric(timestampStr)) {
                log.error("请求参数时间戳格式不正确, timestamp:{}", timestampStr);
                return Mono.error(new ApiTimestampValidException());
            }
            long timestamp = Long.parseLong(timestampStr);
            if (DateUtil.between(new Date(timestamp), new Date(), DateUnit.SECOND)
                    > timestampVerify.getTimestampValidSecond()) {
                log.error("请求参数时间戳校验失败, timestamp:{}", timestamp);
                return Mono.error(new ApiTimestampValidException());
            }
        } catch (Exception e) {
            log.error("请求参数时间戳校验异常, timestamp:{}", timestampStr);
            return Mono.error(new ApiTimestampValidException());
        }

        return chain.filter(exchange);
    }


    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
