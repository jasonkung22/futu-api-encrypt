package cn.futuai.open.encrypt.spring.boot.config;

import cn.futuai.open.encrypt.spring.boot.config.property.ApiEncryptProperties;
import cn.futuai.open.encrypt.spring.boot.filter.request.RequestApiDecryptFilter;
import cn.futuai.open.encrypt.spring.boot.filter.request.RequestApiFilter;
import cn.futuai.open.encrypt.spring.boot.filter.request.RequestApiSignVerifyFilter;
import cn.futuai.open.encrypt.spring.boot.filter.request.RequestApiTimestampVerifyFilter;
import cn.futuai.open.encrypt.spring.boot.filter.response.ResponseEncryptFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * API加密自动配置
 * @author Jason Kung
 * @date 2024/06/07 15:33
 */
@ConditionalOnProperty(name = "spring.api-encrypt.enabled", matchIfMissing = true)
@EnableConfigurationProperties(ApiEncryptProperties.class)
public class ApiEncryptAutoConfiguration {

    @Bean
    public FilterRegistrationBean<RequestApiFilter> requestApiFilterConfig(RequestApiFilter requestApiFilter) {
        FilterRegistrationBean<RequestApiFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(requestApiFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 5);
        registrationBean.setName("requestApiFilter");
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<RequestApiTimestampVerifyFilter> requestApiTimestampVerifyFilterConfig(
            RequestApiTimestampVerifyFilter requestApiTimestampVerifyFilter) {
        FilterRegistrationBean<RequestApiTimestampVerifyFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(requestApiTimestampVerifyFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registrationBean.setName("requestApiTimestampVerifyFilter");
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<RequestApiSignVerifyFilter> requestApiSignVerifyFilterConfig(
            RequestApiSignVerifyFilter requestApiSignVerifyFilter) {
        FilterRegistrationBean<RequestApiSignVerifyFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(requestApiSignVerifyFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        registrationBean.setName("requestApiSignVerifyFilter");
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<RequestApiDecryptFilter> requestApiDecryptFilterConfig(
            RequestApiDecryptFilter requestApiDecryptFilter) {
        FilterRegistrationBean<RequestApiDecryptFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(requestApiDecryptFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 30);
        registrationBean.setName("requestApiDecryptFilter");
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<ResponseEncryptFilter> filterConfig(ResponseEncryptFilter responseEncryptFilter) {
        FilterRegistrationBean<ResponseEncryptFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(responseEncryptFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 40);
        registrationBean.setName("responseEncryptFilter");
        return registrationBean;
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestApiFilter requestApiFilter() {
        return new RequestApiFilter();
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
    public ResponseEncryptFilter responseEncryptFilter() {
        return new ResponseEncryptFilter();
    }
}
