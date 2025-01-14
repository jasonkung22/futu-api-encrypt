package cn.futuai.open.encrypt.spring.boot.filter.request;

import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiTimestampException;
import cn.futuai.open.encrypt.core.property.TimestampVerify;
import cn.futuai.open.encrypt.core.util.ApiChecker;
import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.futuai.open.encrypt.spring.boot.config.property.ApiEncryptProperties;
import cn.hutool.core.util.StrUtil;
import java.io.IOException;
import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 请求时间戳校验
 * @author Jason Kung
 * @date 2024/6/7 11:25
 */
public class RequestApiTimestampVerifyFilter implements Filter {

    @Resource
    private ApiEncryptProperties apiEncryptProperty;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        String requestUri = req.getRequestURI();

        if (ApiChecker.isPass(requestUri, apiEncryptProperty.getEnabled(), apiEncryptProperty.getCheckModel())) {
            chain.doFilter(req, response);
            return;
        }

        String encryptAesKey = req.getHeader(apiEncryptProperty.getEncryptAesKeyHeaderKey());
        if (ApiChecker.isTolerantRequest(requestUri, apiEncryptProperty.getTolerantUrls(), encryptAesKey)) {
            chain.doFilter(req, response);
            return;
        }

        TimestampVerify timestampVerify = apiEncryptProperty.getTimestamp();
        if (!timestampVerify.getEnabled()) {
            chain.doFilter(req, response);
            return;
        }

        String timestampStr = (String) req.getAttribute(ApiEncryptConstant.TIMES_TAMP);

        if (StrUtil.isBlankIfStr(timestampStr) || !StrUtil.isNumeric(timestampStr)) {
            throw new ApiTimestampException(requestUri, timestampStr);
        }
        long timestamp = Long.parseLong(timestampStr);
        if (!ApiEncryptUtil.isTimestampValid(timestamp, timestampVerify.getTimestampValidSecond())) {
            throw new ApiTimestampException(requestUri, timestampStr);
        }

        chain.doFilter(req, response);
    }

}
