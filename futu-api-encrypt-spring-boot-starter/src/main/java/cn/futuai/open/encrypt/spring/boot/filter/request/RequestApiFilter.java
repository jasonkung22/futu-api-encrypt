package cn.futuai.open.encrypt.spring.boot.filter.request;

import cn.futuai.open.encrypt.core.HttpEncryptRequestWrapper;
import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiBaseException;
import cn.futuai.open.encrypt.core.util.ApiChecker;
import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.futuai.open.encrypt.spring.boot.config.property.ApiEncryptProperties;
import cn.futuai.open.encrypt.spring.boot.exception.ApiExceptionHandler;
import cn.hutool.core.util.StrUtil;
import java.io.IOException;
import java.util.Objects;
import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

/**
 * api接口校验和解密过滤器
 * @author Jason Kung
 * @date 2024/06/08 14:28
 */
@Slf4j
public class RequestApiFilter implements Filter {


    @Resource
    private ApiEncryptProperties apiEncryptProperty;

    @Override
    @SneakyThrows({IOException.class, ServletException.class})
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        try {
            String requestUri = req.getRequestURI();
            if (ApiChecker.isPass(requestUri, apiEncryptProperty.getEnabled(), apiEncryptProperty.getCheckModel())) {
                chain.doFilter(req, resp);
                return;
            }

            String encryptAesKey = req.getHeader(apiEncryptProperty.getEncryptAesKeyHeaderKey());

            String sign = req.getHeader(apiEncryptProperty.getSignHeaderKey());
            if (StrUtil.isNotBlank(sign)) {
                req.setAttribute(ApiEncryptConstant.SIGN, sign);
            }
            String timestamp = req.getHeader(apiEncryptProperty.getTimestampHeaderKey());
            if (StrUtil.isNotBlank(timestamp)) {
                req.setAttribute(ApiEncryptConstant.TIMES_TAMP, timestamp);
            }
            String orgQueryString = req.getQueryString();
            if (StrUtil.isNotBlank(orgQueryString)) {
                req.setAttribute(ApiEncryptConstant.ORG_QUERY_STRING, orgQueryString);
            }

            if (StrUtil.isNotBlank(encryptAesKey)) {
                try {
                    String aseKey = ApiEncryptUtil.rsaDecrypt(encryptAesKey);
                    req.setAttribute(ApiEncryptConstant.AES_KEY, aseKey);
                } catch (Exception e) {
                    log.error("对称加密密钥解密失败,requestUri:{},encryptAesKey:{}", requestUri, encryptAesKey, e);
                    throw new ApiBaseException();
                }
            }

            if (Objects.equals(req.getMethod(), HttpMethod.GET.name())) {
                chain.doFilter(req, resp);
                return;
            }

            HttpEncryptRequestWrapper requestWrapper = new HttpEncryptRequestWrapper(req);
            String body = requestWrapper.getBody();
            if (StrUtil.isNotBlank(body)) {
                requestWrapper.setAttribute(ApiEncryptConstant.ORG_BODY, body);
            }
            chain.doFilter(requestWrapper, resp);
        } catch (ApiBaseException e) {
            ApiExceptionHandler.apiExceptionHandler(req, resp, e);
        }
    }
}
