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
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 请求解密过滤器
 * @author Jason Kung
 * @date 2023/10/10 17:12
 */
@Slf4j
public class RequestApiDecryptFilter implements Filter {

    @Resource
    private ApiEncryptProperties apiEncryptProperty;

    @Override
    @SneakyThrows
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String requestUri = req.getRequestURI();

        if (ApiChecker.isPass(requestUri, apiEncryptProperty.getEnabled(), apiEncryptProperty.getCheckModel())) {
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
            log.error("解密请求参数异常,requestUri:{}, queryString:{}, aesKey:{}", requestUri, queryString, aesKey, e);
            throw new ApiDecryptException();
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
            log.error("解密body参数异常,requestUri:{}, orgBody:{}, aesKey:{}", requestUri, orgBody, aesKey, e);
            throw new ApiDecryptException();
        }

        chain.doFilter(requestWrapper, resp);
    }

    @SneakyThrows
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
    @SneakyThrows
    private void updateRequestParam(HttpEncryptRequestWrapper request, Map<String, String> param) {
        if (CollectionUtil.isEmpty(param)) {
            return;
        }

        try {
            request.setParameter(param);
        } catch (Exception e) {
            log.error("修改请求参数异常, param:{}", param, e);
            throw new ApiDecryptException();
        }
    }

}
