package cn.futuai.open.encrypt.spring.cloud.gateway.filter.request;

import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiBaseException;
import cn.futuai.open.encrypt.core.util.ApiChecker;
import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.futuai.open.encrypt.spring.cloud.gateway.config.property.GatewayApiEncryptProperties;
import cn.hutool.core.util.StrUtil;
import java.nio.charset.StandardCharsets;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
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
 * api接口校验和解密过滤器
 * @author Jason Kung
 * @date 2024/06/08 14:28
 */
@Slf4j
@SuppressWarnings("NullableProblems")
public class GatewayRequestApiFilter implements GlobalFilter, Ordered {

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

        String encryptAesKey = request.getHeaders().getFirst(gatewayApiEncryptProperty.getEncryptAesKeyHeaderKey());

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

        if (StrUtil.isNotBlank(encryptAesKey)) {
            try {
                String aseKey = ApiEncryptUtil.rsaDecrypt(encryptAesKey);
                exchange.getAttributes().put(ApiEncryptConstant.AES_KEY, aseKey);
            } catch (Exception e) {
                log.error("对称加密密钥解密失败,requestUri:{},encryptAesKey:{}", requestUri, encryptAesKey, e);
                throw new ApiBaseException();
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
