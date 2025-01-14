package cn.futuai.open.encrypt.spring.boot.filter.response;

import cn.futuai.open.encrypt.core.HttpEncryptResponseWrapper;
import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiEncryptException;
import cn.futuai.open.encrypt.core.property.ResponseEncrypt;
import cn.futuai.open.encrypt.core.util.ApiChecker;
import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.futuai.open.encrypt.spring.boot.config.property.ApiEncryptProperties;
import cn.hutool.core.util.StrUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

/**
 * 返回加密过滤器
 * @author Jason Kung
 * @date 2023/10/10 17:12
 */
public class ResponseEncryptFilter implements Filter {

    @Resource
    private ApiEncryptProperties apiEncryptProperty;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String requestUri = req.getRequestURI();

        if (ApiChecker.isPass(requestUri, apiEncryptProperty.getEnabled(), apiEncryptProperty.getCheckModel())) {
            chain.doFilter(req, response);
            return;
        }

        String encryptAesKey = req.getHeader(apiEncryptProperty.getEncryptAesKeyHeaderKey());
        // 如果是容忍接口且没有加密key，则跳过加密
        if (ApiChecker.isTolerantRequest(requestUri, apiEncryptProperty.getTolerantUrls(), encryptAesKey)) {
            chain.doFilter(req, response);
            return;
        }

        ResponseEncrypt responseEncrypt = apiEncryptProperty.getResponseEncrypt();
        if (ApiChecker.isPass(requestUri, responseEncrypt.getEnabled(), responseEncrypt.getCheckModel())) {
            chain.doFilter(req, response);
            return;
        }

        HttpEncryptResponseWrapper responseWrapper = new HttpEncryptResponseWrapper(resp);

        chain.doFilter(req, responseWrapper);

        String aesKey = (String) request.getAttribute(ApiEncryptConstant.AES_KEY);
        byte[] responseData = responseWrapper.getResponseData();
        if (MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(responseWrapper.getContentType().trim())) {
            try {
                responseData = encryptResponse(responseData, aesKey);
            } catch (Exception e) {
                throw new ApiEncryptException(requestUri, new String(responseData, StandardCharsets.UTF_8), aesKey, e);
            }
        }

        outPut(response, responseData);
    }

    private void outPut(ServletResponse response, byte[] responseData) throws IOException {
        response.setContentLength(responseData.length);
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            out.write(responseData);
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }

    public byte[] encryptResponse(byte[] responseData, String aesKey) {
        String responseBody = new String(responseData, StandardCharsets.UTF_8);
        if (StrUtil.isNotBlank(responseBody) && StrUtil.isNotBlank(aesKey)) {
            String encryptResponse = ApiEncryptUtil.aesEncrypt(responseBody, aesKey);
            if (StrUtil.isNotBlank(encryptResponse)) {
                return encryptResponse.getBytes(StandardCharsets.UTF_8);
            }
        }
        return responseData;
    }

}