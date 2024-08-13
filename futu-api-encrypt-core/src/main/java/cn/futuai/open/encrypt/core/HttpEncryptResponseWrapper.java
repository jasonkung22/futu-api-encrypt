package cn.futuai.open.encrypt.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * http加密响应增强
 * @author Jason Kung
 * @date 2024/8/6 10:01
 */
@Slf4j
public class HttpEncryptResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream outputStream;

    public HttpEncryptResponseWrapper(HttpServletResponse response) {
        super(response);
        this.outputStream = new ByteArrayOutputStream();
    }

    public byte[] getResponseData() {
        try {
            outputStream.flush();
        } catch (IOException e) {
            log.error("get response data error", e);
        }
        return outputStream.toByteArray();
    }

    @Override
    public PrintWriter getWriter() {
        return new PrintWriter(outputStream);
    }


    @Override
    public ServletOutputStream getOutputStream() {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener listener) {

            }

            @Override
            public void write(int b) {
                outputStream.write(b);
            }
        };
    }

}
