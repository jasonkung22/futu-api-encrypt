package cn.futuai.open.encrypt.config;

import cn.futuai.open.encrypt.config.property.GatewayApiEncryptProperty;
import cn.futuai.open.encrypt.exception.handler.GatewayApiValidExceptionHandler;
import cn.futuai.open.encrypt.filter.request.RequestApiDecryptFilter;
import cn.futuai.open.encrypt.filter.request.RequestApiFilter;
import cn.futuai.open.encrypt.filter.request.RequestApiSignVerifyFilter;
import cn.futuai.open.encrypt.filter.request.RequestApiTimestampVerifyFilter;
import cn.futuai.open.encrypt.filter.response.ResponseEncryptFilter;
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
@EnableConfigurationProperties(GatewayApiEncryptProperty.class)
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
    public GatewayApiValidExceptionHandler gatewayApiValidExceptionHandler() {
        return new GatewayApiValidExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestApiTimestampVerifyFilter requestTimestampVerifyFilter() {
        return new RequestApiTimestampVerifyFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestApiSignVerifyFilter requestSignVerifyFilter() {
        return new RequestApiSignVerifyFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestApiDecryptFilter requestDecryptFilter() {
        return new RequestApiDecryptFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestApiFilter requestApiCheckAndDecryptFilter() {
        return new RequestApiFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public ResponseEncryptFilter responseEncryptFilter() {
        return new ResponseEncryptFilter();
    }
}
