package cn.futuai.open.encrypt.spring.boot.filter.request;

import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiSignException;
import cn.futuai.open.encrypt.core.property.SignVerify;
import cn.futuai.open.encrypt.core.util.ApiChecker;
import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.futuai.open.encrypt.spring.boot.config.property.ApiEncryptProperties;
import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 验证签名
 * @author Jason Kung
 * @date 2023/11/07 13:37
 */
@Slf4j
public class RequestApiSignVerifyFilter implements Filter {

    @Resource
    private ApiEncryptProperties apiEncryptProperty;

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletRequest req = (HttpServletRequest) request;

        String requestUri = req.getRequestURI();

        if (ApiChecker.isPass(requestUri, apiEncryptProperty.getEnabled(), apiEncryptProperty.getCheckModel())) {
            chain.doFilter(req, response);
            return;
        }

        SignVerify signVerify = apiEncryptProperty.getSign();
        if (!signVerify.getEnabled()) {
            chain.doFilter(request, response);
            return;
        }

        String sign = (String) req.getAttribute(ApiEncryptConstant.SIGN);
        String aesKey = (String) req.getAttribute(ApiEncryptConstant.AES_KEY);
        String timestamp = (String) req.getAttribute(ApiEncryptConstant.TIMES_TAMP);
        String orgQueryString = (String) req.getAttribute(ApiEncryptConstant.ORG_QUERY_STRING);
        String orgBody = (String) req.getAttribute(ApiEncryptConstant.ORG_BODY);

        if (!ApiEncryptUtil.verifySign(timestamp, aesKey, orgQueryString, orgBody, sign)) {
            log.error("请求参数验签失败,requestUri:{}, timestamp:{}, aesKey:{}, orgQueryString:{}, orgBody:{}, sign:{}",
                    requestUri, timestamp, aesKey, orgQueryString, orgBody, sign);
            throw new ApiSignException();
        }
        chain.doFilter(req, response);
    }

}
