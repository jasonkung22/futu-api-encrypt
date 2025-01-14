package cn.futuai.open.encrypt.spring.boot.filter.request;

import cn.futuai.open.encrypt.core.HttpEncryptRequestWrapper;
import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiDecryptException;
import cn.futuai.open.encrypt.core.property.RequestDecrypt;
import cn.futuai.open.encrypt.core.util.ApiChecker;
import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.futuai.open.encrypt.spring.boot.config.property.ApiEncryptProperties;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 请求解密过滤器
 * @author Jason Kung
 * @date 2023/10/10 17:12
 */
public class RequestApiDecryptFilter implements Filter {

    @Resource
    private ApiEncryptProperties apiEncryptProperty;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String requestUri = req.getRequestURI();

        if (ApiChecker.isPass(requestUri, apiEncryptProperty.getEnabled(), apiEncryptProperty.getCheckModel())) {
            chain.doFilter(req, resp);
            return;
        }

        String encryptAesKey = req.getHeader(apiEncryptProperty.getEncryptAesKeyHeaderKey());
        // 如果是容忍接口且没有加密key，则跳过解密
        if (ApiChecker.isTolerantRequest(requestUri, apiEncryptProperty.getTolerantUrls(), encryptAesKey)) {
            chain.doFilter(req, resp);
            return;
        }

        RequestDecrypt requestDecrypt = apiEncryptProperty.getRequestDecrypt();

        if (ApiChecker.isPass(requestUri, requestDecrypt.getEnabled(), requestDecrypt.getCheckModel())) {
            chain.doFilter(req, resp);
            return;
        }

        HttpEncryptRequestWrapper requestWrapper = new HttpEncryptRequestWrapper(req);
        String queryString = (String) requestWrapper.getAttribute(ApiEncryptConstant.ORG_QUERY_STRING);
        String aesKey = (String) requestWrapper.getAttribute(ApiEncryptConstant.AES_KEY);
        try {
            Map<String, String> decryptRequestParam = decryptRequestParam(queryString, aesKey);
            updateRequestParam(requestWrapper, decryptRequestParam);
        } catch (Exception e) {
            throw new ApiDecryptException(requestUri, queryString, aesKey, e);
        }

        String orgBody = (String) requestWrapper.getAttribute(ApiEncryptConstant.ORG_BODY);
        if (StrUtil.isBlank(orgBody)) {
            chain.doFilter(requestWrapper, resp);
            return;
        }

        try {
            String body = ApiEncryptUtil.aesDecrypt(orgBody, aesKey);
            requestWrapper.setBody(body);
        } catch (Exception e) {
            throw new ApiDecryptException(requestUri, queryString, aesKey, e);
        }

        chain.doFilter(requestWrapper, resp);
    }

    private Map<String, String> decryptRequestParam(String queryString, String aesKey) {
        Map<String, String> paramMap = new HashMap<>();
        if (StrUtil.isNotBlank(queryString) && queryString.contains(
                apiEncryptProperty.getEncryptParamKey())) {
            String[] split = queryString.split("=");
            String paramValue = split[1];
            //解密请求参数
            String decryptQueryString = ApiEncryptUtil.aesDecrypt(paramValue, aesKey);
            if (StrUtil.isBlank(decryptQueryString)) {
                return paramMap;
            }
            String[] paramsArray = decryptQueryString.split("&");
            for (String paramStr : paramsArray) {
                String[] param = paramStr.split("=");
                paramMap.put(param[0], param[1]);
            }
            return paramMap;
        }
        return paramMap;
    }

    /**
     * 修改前端传的参数
     */
    private void updateRequestParam(HttpEncryptRequestWrapper request, Map<String, String> param) {
        if (CollectionUtil.isEmpty(param)) {
            return;
        }

        request.setParameter(param);
    }

}
