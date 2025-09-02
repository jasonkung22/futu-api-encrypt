package cn.futuai.open.encrypt.spring.boot.filter.request;

import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiSignException;
import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.futuai.open.encrypt.spring.boot.config.property.ApiEncryptProperties;
import cn.futuai.open.encrypt.spring.boot.filter.AbstractApiFilter;
import java.io.IOException;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 验证签名
 * @author Jason Kung
 * @date 2023/11/07 13:37
 */
public class RequestApiSignVerifyFilter extends AbstractApiFilter {

    @Resource
    private ApiEncryptProperties apiEncryptProperty;

    @Override
    protected ApiEncryptProperties getApiEncryptProperty() {
        return apiEncryptProperty;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        String sign = (String) request.getAttribute(ApiEncryptConstant.SIGN);
        String aesKey = (String) request.getAttribute(ApiEncryptConstant.AES_KEY);
        String timestamp = (String) request.getAttribute(ApiEncryptConstant.TIMES_TAMP);
        String orgQueryString = (String) request.getAttribute(ApiEncryptConstant.ORG_QUERY_STRING);
        String orgBody = (String) request.getAttribute(ApiEncryptConstant.ORG_BODY);

        if (!ApiEncryptUtil.verifySign(timestamp, aesKey, orgQueryString, orgBody, sign)) {
            throw new ApiSignException(requestUri, timestamp, aesKey, orgQueryString, orgBody, sign);
        }
        chain.doFilter(request, response);
    }
}
