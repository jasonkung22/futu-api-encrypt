package cn.futuai.open.encrypt.spring.cloud.gateway.filter.request;

import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiSignException;
import cn.futuai.open.encrypt.core.property.SignVerify;
import cn.futuai.open.encrypt.core.util.ApiChecker;
import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.futuai.open.encrypt.spring.cloud.gateway.config.property.GatewayApiEncryptProperties;
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
 * 验证签名
 * @author Jason Kung
 * @date 2023/11/07 13:37
 */
@Slf4j
public class GatewayRequestApiSignVerifyFilter implements GlobalFilter, Ordered {

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

        SignVerify signVerify = gatewayApiEncryptProperty.getSign();
        if (!signVerify.getEnabled()) {
            return chain.filter(exchange);
        }

        String sign = exchange.getAttribute(ApiEncryptConstant.SIGN);
        String aesKey = exchange.getAttribute(ApiEncryptConstant.AES_KEY);
        String timestamp = exchange.getAttribute(ApiEncryptConstant.TIMES_TAMP);
        String orgQueryString = exchange.getAttribute(ApiEncryptConstant.ORG_QUERY_STRING);
        String orgBody = exchange.getAttribute(ApiEncryptConstant.ORG_BODY);

        if (!ApiEncryptUtil.verifySign(timestamp, aesKey, orgQueryString, orgBody, sign)) {
            log.error("请求参数验签失败,requestUri:{}, timestamp:{}, aesKey:{}, orgQueryString:{}, orgBody:{}, sign:{}",
                    requestUri, timestamp, aesKey, orgQueryString, orgBody, sign);
            throw new ApiSignException();
        }
        return chain.filter(exchange);
    }


    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}
