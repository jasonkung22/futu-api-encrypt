package cn.futuai.open.encrypt.core;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义request增强类
 * @author Jason Kung
 * @date 2024/08/05 15:29
 */
@Slf4j
public class HttpEncryptRequestWrapper extends HttpServletRequestWrapper {

    /**
     * 保存request body的数据
     */
    private String body;

    private Map<String, String> paramMap = new HashMap<>();

    private final Map<String, String> headerMap = new HashMap<>();

    /**
     * 解析request的inputStream(即body)数据，转成字符串
     * @param request 请求
     */
    public HttpEncryptRequestWrapper(HttpServletRequest request) {
        super(request);
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        InputStream inputStream = null;
        try {
            inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            }
        } catch (IOException ex) {
            log.error("io exception error", ex);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        body = stringBuilder.toString();
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes());
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        };

    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void addParameter(String name, String value) {
        this.paramMap.put(name, value);
    }

    public void setParameter(Map<String, String> paramMap) {
        this.paramMap = paramMap;
    }

    @Override
    public String getParameter(String name) {
        if (paramMap.containsKey(name)) {
            return paramMap.get(name);
        }
        return super.getParameter(name);
    }

    @Override
    public String[] getParameterValues(String name) {
        if (paramMap.containsKey(name)) {
            return new String[]{getParameter(name)};
        }
        return super.getParameterValues(name);
    }

    /**
     * add a header with given name and value
     * @param name  key
     * @param value value
     */
    public void addHeader(String name, String value) {
        headerMap.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        if (headerMap.containsKey(name)) {
            return headerMap.get(name);
        }
        return super.getHeader(name);
    }

    /**
     * get the Header names
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> names = Collections.list(super.getHeaderNames());
        names.addAll(headerMap.keySet());
        return Collections.enumeration(names);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> values = Collections.list(super.getHeaders(name));
        if (headerMap.containsKey(name)) {
            values.add(headerMap.get(name));
        }
        return Collections.enumeration(values);
    }
}

