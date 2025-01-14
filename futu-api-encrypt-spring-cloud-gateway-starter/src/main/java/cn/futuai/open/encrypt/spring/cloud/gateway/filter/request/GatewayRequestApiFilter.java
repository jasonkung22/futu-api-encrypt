package cn.futuai.open.encrypt.spring.cloud.gateway.filter.request;

import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiDecryptException;
import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.futuai.open.encrypt.spring.cloud.gateway.config.property.GatewayApiEncryptProperties;
import cn.futuai.open.encrypt.spring.cloud.gateway.filter.AbstractGatewayFilter;
import cn.hutool.core.util.StrUtil;
import java.nio.charset.StandardCharsets;
import javax.annotation.Resource;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * API接口校验和解密过滤器
 * 作为入口过滤器，主要负责：
 * 1. 提取和设置请求相关的属性（签名、时间戳、请求参数等）
 * 2. 解密AES密钥
 * 3. 统一的异常处理
 * @author Jason Kung
 * @date 2024/06/08 14:28
 */
@SuppressWarnings("NullableProblems")
public class GatewayRequestApiFilter extends AbstractGatewayFilter {

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

        String sign = request.getHeaders().getFirst(gatewayApiEncryptProperty.getSignHeaderKey());
        if (StrUtil.isNotBlank(sign)) {
            exchange.getAttributes().put(ApiEncryptConstant.SIGN, sign);
        }
        String timestamp = request.getHeaders().getFirst(gatewayApiEncryptProperty.getTimestampHeaderKey());
        if (StrUtil.isNotBlank(timestamp)) {
            exchange.getAttributes().put(ApiEncryptConstant.TIMES_TAMP, timestamp);
        }
        String orgQueryString = request.getURI().getQuery();
        if (StrUtil.isNotBlank(orgQueryString)) {
            exchange.getAttributes().put(ApiEncryptConstant.ORG_QUERY_STRING, orgQueryString);
        }

        String encryptAesKey = request.getHeaders().getFirst(gatewayApiEncryptProperty.getEncryptAesKeyHeaderKey());
        if (StrUtil.isNotBlank(encryptAesKey)) {
            try {
                String aseKey = ApiEncryptUtil.rsaDecrypt(encryptAesKey);
                exchange.getAttributes().put(ApiEncryptConstant.AES_KEY, aseKey);
            } catch (Exception e) {
                throw new ApiDecryptException(requestUri, encryptAesKey, "", e);
            }
        }

        if (request.getMethod() == HttpMethod.GET) {
            return chain.filter(exchange);
        }

        DefaultDataBufferFactory defaultDataBufferFactory = new DefaultDataBufferFactory();
        DefaultDataBuffer defaultDataBuffer = defaultDataBufferFactory.allocateBuffer(0);

        return DataBufferUtils.join(request.getBody().defaultIfEmpty(defaultDataBuffer))
                .flatMap(dataBuffer -> {
                    DataBufferUtils.retain(dataBuffer);
                    Flux<DataBuffer> cachedFlux = Flux
                            .defer(() -> Flux.just(dataBuffer.slice(0, dataBuffer.readableByteCount())));
                    ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(request) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            return cachedFlux;
                        }
                    };

                    return getBody(exchange.mutate().request(mutatedRequest).build(), chain);
                });
    }

    private Mono<Void> getBody(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取请求参数
        //重置request

        // 获取请求体数据
        Mono<String> bodyMono = exchange.getRequest().getBody()
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return new String(bytes, StandardCharsets.UTF_8);
                }).reduce(String::concat);
        return bodyMono.flatMap(body -> {
            if (StrUtil.isNotBlank(body)) {
                exchange.getAttributes().put(ApiEncryptConstant.ORG_BODY, body);
            }
            return chain.filter(exchange);
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 5;
    }
}
