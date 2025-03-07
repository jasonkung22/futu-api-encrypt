package cn.futuai.open.encrypt.spring.boot.filter;

import cn.futuai.open.encrypt.core.util.ApiChecker;
import cn.futuai.open.encrypt.spring.boot.config.property.ApiEncryptProperties;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * API过滤器基类，提供通用的过滤器处理逻辑
 * 实现了模板方法模式，定义了过滤器的基本骨架：
 * 1. 全局开关检查
 * 2. 容忍模式检查
 * 3. 具体业务逻辑
 * @author Jason Kung
 * @date 2024/06/08 14:28
 */
public abstract class AbstractApiFilter implements Filter {

    /**
     * 获取API加密配置属性
     * @return API加密配置属性
     */
    protected abstract ApiEncryptProperties getApiEncryptProperty();

    /**
     * 执行具体的过滤器逻辑
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param chain    过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException      IO异常
     */
    protected abstract void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String requestUri = req.getRequestURI();
        // 全局开关检查
        if (ApiChecker.isPass(requestUri, getApiEncryptProperty().getEnabled(),
                getApiEncryptProperty().getCheckModel())) {
            chain.doFilter(req, resp);
            return;
        }

        // 容忍模式检查
        // 先从请求属性中获取容忍模式判断结果
        Boolean isTolerantRequest = (Boolean) req.getAttribute(ApiChecker.TOLERANT_REQUEST_ATTRIBUTE);
        
        // 如果请求属性中没有判断结果，执行判断并存入请求属性
        if (isTolerantRequest == null) {
            String encryptAesKey = req.getHeader(getApiEncryptProperty().getEncryptAesKeyHeaderKey());
            isTolerantRequest = ApiChecker.isTolerantRequest(requestUri, 
                    getApiEncryptProperty().getTolerantUrls(), encryptAesKey);
            req.setAttribute(ApiChecker.TOLERANT_REQUEST_ATTRIBUTE, isTolerantRequest);
        }
        
        if (isTolerantRequest) {
            chain.doFilter(req, resp);
            return;
        }

        // 执行具体过滤器逻辑
        doFilterInternal(req, resp, chain);
    }
} 