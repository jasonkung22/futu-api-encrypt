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
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

/**
 * 返回加密过滤器
 * @author Jason Kung
 * @date 2023/10/10 17:12
 */
@Slf4j
public class ResponseEncryptFilter implements Filter {

    @Resource
    private ApiEncryptProperties apiEncryptProperty;

    @Override
    @SneakyThrows
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String requestUri = req.getRequestURI();

        if (ApiChecker.isPass(requestUri, apiEncryptProperty.getEnabled(), apiEncryptProperty.getCheckModel())) {
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

        try {
            byte[] responseData = responseWrapper.getResponseData();
            if (MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(responseWrapper.getContentType().trim())) {
                String aesKey = (String) request.getAttribute(ApiEncryptConstant.AES_KEY);
                responseData = encryptResponse(responseData, aesKey);
            }
            outPut(response, responseData);
        } catch (Exception e) {
            log.error("响应结果加密异常,requestUri:{}", requestUri, e);
            throw new ApiEncryptException();
        }
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

    @SneakyThrows
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