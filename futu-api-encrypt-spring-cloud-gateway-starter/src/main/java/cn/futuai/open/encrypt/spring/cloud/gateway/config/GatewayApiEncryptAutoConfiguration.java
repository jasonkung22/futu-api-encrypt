package cn.futuai.open.encrypt.spring.cloud.gateway.config;

import cn.futuai.open.encrypt.spring.cloud.gateway.config.property.GatewayApiEncryptProperties;
import cn.futuai.open.encrypt.spring.cloud.gateway.exception.GatewayApiExceptionHandler;
import cn.futuai.open.encrypt.spring.cloud.gateway.filter.request.GatewayRequestApiDecryptFilter;
import cn.futuai.open.encrypt.spring.cloud.gateway.filter.request.GatewayRequestApiFilter;
import cn.futuai.open.encrypt.spring.cloud.gateway.filter.request.GatewayRequestApiSignVerifyFilter;
import cn.futuai.open.encrypt.spring.cloud.gateway.filter.request.GatewayRequestApiTimestampVerifyFilter;
import cn.futuai.open.encrypt.spring.cloud.gateway.filter.response.GatewayResponseEncryptFilter;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

/**
 * API加密网关自动配置
 * @author Jason Kung
 * @date 2024/06/07 15:33
 */
@ConditionalOnProperty(name = "spring.cloud.gateway.api-encrypt.enabled", matchIfMissing = true)
@EnableConfigurationProperties(GatewayApiEncryptProperties.class)
public class GatewayApiEncryptAutoConfiguration {


    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public GatewayApiEncryptAutoConfiguration(
            ObjectProvider<List<ViewResolver>> viewResolversProvider,
            ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;

    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GatewayApiExceptionHandler gatewayApiValidExceptionHandler() {
        return new GatewayApiExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    @Bean
    @ConditionalOnMissingBean
    public GatewayRequestApiFilter requestApiFilter() {
        return new GatewayRequestApiFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public GatewayRequestApiTimestampVerifyFilter requestTimestampVerifyFilter() {
        return new GatewayRequestApiTimestampVerifyFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public GatewayRequestApiSignVerifyFilter requestSignVerifyFilter() {
        return new GatewayRequestApiSignVerifyFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public GatewayRequestApiDecryptFilter requestDecryptFilter() {
        return new GatewayRequestApiDecryptFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public GatewayResponseEncryptFilter responseEncryptFilter() {
        return new GatewayResponseEncryptFilter();
    }
}
