package cn.futuai.open.encrypt.spring.boot.filter.request;

import cn.futuai.open.encrypt.core.HttpEncryptRequestWrapper;
import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiDecryptException;
import cn.futuai.open.encrypt.core.property.RequestDecrypt;
import cn.futuai.open.encrypt.core.util.ApiChecker;
import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.futuai.open.encrypt.spring.boot.config.property.ApiEncryptProperties;
import cn.futuai.open.encrypt.spring.boot.filter.AbstractApiFilter;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 请求解密过滤器
 * 主要职责：
 * 1. 解密请求参数
 * 2. 解密请求体
 * 3. 更新请求参数
 * 4. 参数安全校验
 * @author Jason Kung
 * @date 2023/10/10 17:12
 */
public class RequestApiDecryptFilter extends AbstractApiFilter {

    @Resource
    private ApiEncryptProperties apiEncryptProperty;

    @Override
    protected ApiEncryptProperties getApiEncryptProperty() {
        return apiEncryptProperty;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        String requestUri = req.getRequestURI();

        // 检查是否在解密接口列表中
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
     * 更新请求参数
     * @param request 请求包装器
     * @param param   解密后的参数Map
     */
    private void updateRequestParam(HttpEncryptRequestWrapper request, Map<String, String> param) {
        if (CollectionUtil.isEmpty(param)) {
            return;
        }

        request.setParameter(param);
    }
}
