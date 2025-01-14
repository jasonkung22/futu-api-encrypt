package cn.futuai.open.encrypt.spring.boot.filter.response;

import cn.futuai.open.encrypt.core.HttpEncryptResponseWrapper;
import cn.futuai.open.encrypt.core.constants.ApiEncryptConstant;
import cn.futuai.open.encrypt.core.exception.ApiEncryptException;
import cn.futuai.open.encrypt.core.property.ResponseEncrypt;
import cn.futuai.open.encrypt.core.util.ApiChecker;
import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.futuai.open.encrypt.spring.boot.config.property.ApiEncryptProperties;
import cn.futuai.open.encrypt.spring.boot.filter.AbstractApiFilter;
import cn.hutool.core.util.StrUtil;
import java.io.IOException;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

/**
 * 返回加密过滤器
 * @author Jason Kung
 * @date 2023/10/10 17:12
 */
public class ResponseEncryptFilter extends AbstractApiFilter {

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

        // 检查是否在加密接口列表中
        ResponseEncrypt responseEncrypt = apiEncryptProperty.getResponseEncrypt();
        if (ApiChecker.isPass(requestUri, responseEncrypt.getEnabled(), responseEncrypt.getCheckModel())) {
            chain.doFilter(request, response);
            return;
        }

        HttpEncryptResponseWrapper responseWrapper = new HttpEncryptResponseWrapper(response);

        chain.doFilter(request, responseWrapper);

        String aesKey = (String) request.getAttribute(ApiEncryptConstant.AES_KEY);
        byte[] responseData = responseWrapper.getResponseData();
        if (MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(responseWrapper.getContentType().trim())) {
            try {
                responseData = encryptResponse(responseData, aesKey, requestUri);
            } catch (Exception e) {
                throw new ApiEncryptException(requestUri, new String(responseData), aesKey, e);
            }
        }

        outPut(response, responseData);
    }

    private void outPut(HttpServletResponse response, byte[] responseData) throws IOException {
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

    private byte[] encryptResponse(byte[] responseData, String aesKey, String requestUri) {
        String responseBody = new String(responseData);
        if (StrUtil.isNotBlank(responseBody) && StrUtil.isNotBlank(aesKey)) {
            String encryptResponse = ApiEncryptUtil.aesEncrypt(responseBody, aesKey);
            if (StrUtil.isNotBlank(encryptResponse)) {
                return encryptResponse.getBytes();
            }
        }
        return responseData;
    }
}