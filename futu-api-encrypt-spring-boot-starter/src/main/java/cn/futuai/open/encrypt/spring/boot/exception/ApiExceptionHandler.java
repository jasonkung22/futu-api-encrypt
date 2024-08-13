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
package cn.futuai.open.encrypt.spring.boot.exception;

import cn.futuai.open.encrypt.core.exception.ApiBaseException;
import cn.futuai.open.encrypt.spring.boot.callback.ApiExceptionCallbackManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

/**
 * 网关api异常处理器
 * @author Jason Kung
 * @date 2024/6/7 15:21
 */
public class ApiExceptionHandler {

    @SneakyThrows
    public static void apiExceptionHandler(HttpServletRequest request, HttpServletResponse response,
            ApiBaseException e) {
        ApiExceptionCallbackManager.getApiExceptionHandler().handleRequest(request, response, e);
    }
}
