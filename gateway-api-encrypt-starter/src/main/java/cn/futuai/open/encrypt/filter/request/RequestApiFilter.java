package cn.futuai.open.encrypt.filter.request;

import cn.futuai.open.encrypt.config.property.GatewayApiEncryptProperty;
import cn.futuai.open.encrypt.config.property.GatewayApiEncryptProperty.CheckModel;
import cn.futuai.open.encrypt.config.property.GatewayApiEncryptProperty.CheckModelEnum;
import cn.futuai.open.encrypt.exception.ApiValidException;
import cn.futuai.open.encrypt.util.ApiEncryptUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.annotation.Resource;
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
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * api接口校验和解密过滤器
 * @author gyf
 * @date 2024/06/08 14:28
 */
@Slf4j
public class RequestApiFilter implements GlobalFilter, Ordered {

    /**
     * Aes密钥
     */
    public final static String AES_KEY = "aesKey";
    /**
     * 签名
     */
    public final static String SIGN = "sign";
    /**
     * 时间戳
     */
    public final static String TIMES_TAMP = "timestamp";
    /**
     * 原始请求参数
     */
    public final static String ORG_QUERY_STRING = "orgQueryString";
    /**
     * 原始请求体
     */
    public final static String ORG_BODY = "orgBody";

    private final static AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    @Resource
    private GatewayApiEncryptProperty gatewayApiEncryptProperty;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!gatewayApiEncryptProperty.getEnabled()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        if (isPass(request, gatewayApiEncryptProperty.getCheckModel())) {
            return chain.filter(exchange);
        }

        String encryptAesKey = request.getHeaders().getFirst(gatewayApiEncryptProperty.getEncryptAesKeyHeaderKey());

        String sign = request.getHeaders().getFirst(gatewayApiEncryptProperty.getSignHeaderKey());
        if (StrUtil.isNotBlank(sign)) {
            exchange.getAttributes().put(SIGN, sign);
        }
        String timestamp = request.getHeaders().getFirst(gatewayApiEncryptProperty.getTimestampHeaderKey());
        if (StrUtil.isNotBlank(timestamp)) {
            exchange.getAttributes().put(TIMES_TAMP, timestamp);
        }
        String orgQueryString = request.getURI().getQuery();
        if (StrUtil.isNotBlank(orgQueryString)) {
            exchange.getAttributes().put(ORG_QUERY_STRING, orgQueryString);
        }

        if (StrUtil.isNotBlank(encryptAesKey)) {
            try {
                String aseKey = ApiEncryptUtil.rsaDecrypt(encryptAesKey);
                exchange.getAttributes().put(AES_KEY, aseKey);
            } catch (Exception e) {
                log.error("对称加密密钥解密失败,encryptAesKey:{}", encryptAesKey, e);
                return Mono.error(new ApiValidException());
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
                exchange.getAttributes().put(ORG_BODY, body);
            }
            return chain.filter(exchange);
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 5;
    }


    public static boolean isMatchUrl(String url, List<String> urlList) {
        if (StrUtil.isEmpty(url)) {
            return false;
        }

        if (CollectionUtil.isEmpty(urlList)) {
            return false;
        }

        for (String item : urlList) {
            if (ANT_PATH_MATCHER.match(item, url)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isPass(ServerHttpRequest request, CheckModel checkModel) {
        String url = request.getURI().getPath();

        boolean isPass = CheckModelEnum.WHITE_LIST.equals(checkModel.getModel())
                && isMatchUrl(url, checkModel.getWhiteList());

        if (CheckModelEnum.BLACK_LIST.equals(checkModel.getModel())
                && !isMatchUrl(url, checkModel.getBlackList())) {
            isPass = true;
        }

        return isPass;
    }
}
