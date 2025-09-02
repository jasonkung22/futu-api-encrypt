package cn.futuai.open.encrypt.spring.boot.filter.request;

import cn.futuai.open.encrypt.core.HttpEncryptRequestWrapper;
import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiBaseException;
import cn.futuai.open.encrypt.core.exception.ApiDecryptException;
import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.futuai.open.encrypt.spring.boot.config.property.ApiEncryptProperties;
import cn.futuai.open.encrypt.spring.boot.exception.ApiExceptionHandler;
import cn.futuai.open.encrypt.spring.boot.filter.AbstractApiFilter;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;

/**
 * API接口校验和解密过滤器
 * 作为入口过滤器，主要负责：
 * 1. 提取和设置请求相关的属性（签名、时间戳、请求参数等）
 * 2. 解密AES密钥
 * 3. 统一的异常处理
 * @author Jason Kung
 * @date 2024/06/08 14:28
 */
public class RequestApiFilter extends AbstractApiFilter {

    @Resource
    private ApiEncryptProperties apiEncryptProperty;
    @Resource
    private ApiExceptionHandler apiExceptionHandler;

    @Override
    protected ApiEncryptProperties getApiEncryptProperty() {
        return apiEncryptProperty;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String requestUri = request.getRequestURI();
            String sign = request.getHeader(apiEncryptProperty.getSignHeaderKey());
            if (StrUtil.isNotBlank(sign)) {
                request.setAttribute(ApiEncryptConstant.SIGN, sign);
            }
            String timestamp = request.getHeader(apiEncryptProperty.getTimestampHeaderKey());
            if (StrUtil.isNotBlank(timestamp)) {
                request.setAttribute(ApiEncryptConstant.TIMES_TAMP, timestamp);
            }
            String orgQueryString = request.getQueryString();
            if (StrUtil.isNotBlank(orgQueryString)) {
                orgQueryString = urlDecode(orgQueryString);
                request.setAttribute(ApiEncryptConstant.ORG_QUERY_STRING, orgQueryString);
            }

            String encryptAesKey = request.getHeader(apiEncryptProperty.getEncryptAesKeyHeaderKey());
            if (StrUtil.isNotBlank(encryptAesKey)) {
                try {
                    String aseKey = ApiEncryptUtil.rsaDecrypt(encryptAesKey);
                    request.setAttribute(ApiEncryptConstant.AES_KEY, aseKey);
                } catch (Exception e) {
                    throw new ApiDecryptException(requestUri, encryptAesKey, "", e);
                }
            }

            if (Objects.equals(request.getMethod(), HttpMethod.GET.name())) {
                chain.doFilter(request, response);
                return;
            }

            HttpEncryptRequestWrapper requestWrapper = new HttpEncryptRequestWrapper(request);
            String body = requestWrapper.getBody();
            if (StrUtil.isNotBlank(body)) {
                requestWrapper.setAttribute(ApiEncryptConstant.ORG_BODY, body);
            }
            chain.doFilter(requestWrapper, response);
        } catch (ApiBaseException e) {
            apiExceptionHandler.apiExceptionHandler(request, response, e);
        }
    }

    public String urlDecode(String url) {
        if (url == null) {
            return null;
        }
        int n = url.length();
        if (n == 0) {
            return url;
        }
        if (url.indexOf('%') < 0) {
            return url;
        }
        return URLUtil.decode(url, Charset.defaultCharset());
    }
}
