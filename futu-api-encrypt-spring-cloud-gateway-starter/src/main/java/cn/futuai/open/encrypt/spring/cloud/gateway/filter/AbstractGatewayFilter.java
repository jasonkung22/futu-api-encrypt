package cn.futuai.open.encrypt.spring.cloud.gateway.filter;

import cn.futuai.open.encrypt.core.util.ApiChecker;
import cn.futuai.open.encrypt.spring.cloud.gateway.config.property.GatewayApiEncryptProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * API网关过滤器基类，提供通用的过滤器处理逻辑
 * 实现了模板方法模式，定义了过滤器的基本骨架：
 * 1. 全局开关检查
 * 2. 容忍模式检查
 * 3. 具体业务逻辑
 * @author Jason Kung
 * @date 2024/06/08 14:28
 */
public abstract class AbstractGatewayFilter implements GlobalFilter, Ordered {

    /**
     * 获取API加密配置属性
     * @return API加密配置属性
     */
    protected abstract GatewayApiEncryptProperties getGatewayApiEncryptProperty();

    /**
     * 执行具体的过滤器逻辑
     * @param exchange 服务器Web交换
     * @param chain    网关过滤器链
     * @return 结果
     */
    protected abstract Mono<Void> doFilterInternal(ServerWebExchange exchange, GatewayFilterChain chain);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestUri = exchange.getRequest().getURI().getPath();
        // 全局开关检查
        GatewayApiEncryptProperties gatewayApiEncryptProperty = getGatewayApiEncryptProperty();
        if (ApiChecker.isPass(requestUri, gatewayApiEncryptProperty.getEnabled(),
                gatewayApiEncryptProperty.getCheckModel())) {
            return chain.filter(exchange);
        }

        // 容忍模式检查
        String encryptAesKey = exchange.getRequest().getHeaders()
                .getFirst(gatewayApiEncryptProperty.getEncryptAesKeyHeaderKey());
        if (ApiChecker.isTolerantRequest(requestUri, gatewayApiEncryptProperty.getTolerantUrls(), encryptAesKey)) {
            return chain.filter(exchange);
        }

        // 执行具体过滤器逻辑
        return doFilterInternal(exchange, chain);
    }
} 