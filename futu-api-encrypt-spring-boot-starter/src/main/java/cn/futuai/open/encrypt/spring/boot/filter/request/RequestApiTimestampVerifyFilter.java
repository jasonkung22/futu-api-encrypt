package cn.futuai.open.encrypt.spring.boot.filter.request;

import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiTimestampException;
import cn.futuai.open.encrypt.core.property.TimestampVerify;
import cn.futuai.open.encrypt.core.util.ApiChecker;
import cn.futuai.open.encrypt.spring.boot.config.property.ApiEncryptProperties;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import java.util.Date;
import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 请求时间戳校验
 * @author Jason Kung
 * @date 2024/6/7 11:25
 */
@Slf4j
public class RequestApiTimestampVerifyFilter implements Filter {

    @Resource
    private ApiEncryptProperties apiEncryptProperty;

    @Override
    @SneakyThrows
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletRequest req = (HttpServletRequest) request;
        String requestUri = req.getRequestURI();

        if (ApiChecker.isPass(requestUri, apiEncryptProperty.getEnabled(), apiEncryptProperty.getCheckModel())) {
            chain.doFilter(req, response);
            return;
        }

        TimestampVerify timestampVerify = apiEncryptProperty.getTimestamp();
        if (!timestampVerify.getEnabled()) {
            chain.doFilter(request, response);
            return;
        }

        String timestampStr = (String) req.getAttribute(ApiEncryptConstant.TIMES_TAMP);

        if (StrUtil.isBlankIfStr(timestampStr) || !StrUtil.isNumeric(timestampStr)) {
            log.error("请求参数时间戳格式不正确,requestUri:{}, timestamp:{}", requestUri, timestampStr);
            throw new ApiTimestampException();
        }
        long timestamp = Long.parseLong(timestampStr);
        if (DateUtil.between(new Date(timestamp), new Date(), DateUnit.SECOND)
                > timestampVerify.getTimestampValidSecond()) {
            log.error("请求参数时间戳校验失败,requestUri:{}, timestamp:{}", requestUri, timestamp);
            throw new ApiTimestampException();
        }

        chain.doFilter(req, response);
    }

}
