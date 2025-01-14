package cn.futuai.open.encrypt.spring.boot.filter.request;

import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiTimestampException;
import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.futuai.open.encrypt.spring.boot.config.property.ApiEncryptProperties;
import cn.futuai.open.encrypt.spring.boot.filter.AbstractApiFilter;
import cn.hutool.core.util.StrUtil;
import java.io.IOException;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 请求时间戳校验
 * @author Jason Kung
 * @date 2024/6/7 11:25
 */
public class RequestApiTimestampVerifyFilter extends AbstractApiFilter {

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
        String timestampStr = (String) request.getAttribute(ApiEncryptConstant.TIMES_TAMP);

        if (StrUtil.isBlankIfStr(timestampStr) || !StrUtil.isNumeric(timestampStr)) {
            throw new ApiTimestampException(requestUri, timestampStr);
        }
        long timestamp = Long.parseLong(timestampStr);
        if (!ApiEncryptUtil.isTimestampValid(timestamp, apiEncryptProperty.getTimestamp().getTimestampValidSecond())) {
            throw new ApiTimestampException(requestUri, timestampStr);
        }

        chain.doFilter(request, response);
    }
}
