/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.futuai.open.encrypt.spring.boot.exception.handler.impl;


import cn.futuai.open.encrypt.spring.boot.exception.handler.ApiExceptionRequestHandler;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;

/**
 * 默认api请求异常处理器
 * @author Jason Kung
 * @date 2024/6/7 15:05
 */
public class DefaultApiExceptionRequestHandler implements ApiExceptionRequestHandler {

    private static final String DEFAULT_EXCEPTION_MSG = "invalid param: ";

    @SneakyThrows
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response, Throwable e) {
        // Return 403 (Request Forbidden) by default.
        response.setStatus(HttpStatus.FORBIDDEN.value());

        PrintWriter out = response.getWriter();
        out.print(DEFAULT_EXCEPTION_MSG + e.getClass().getSimpleName());
        out.flush();
        out.close();
    }
}
