/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.futuai.open.encrypt.spring.boot.callback;

import cn.futuai.open.encrypt.spring.boot.exception.handler.ApiExceptionRequestHandler;
import cn.futuai.open.encrypt.spring.boot.exception.handler.impl.DefaultApiExceptionRequestHandler;
import cn.hutool.core.lang.Assert;

/**
 * 网关加密回调管理器
 * @author Jason Kung
 * @date 2024/6/7 14:15
 */
public final class ApiExceptionCallbackManager {

    /**
     * ApiRequestExceptionHandler: (serverExchange, exception) -> response
     */
    private static volatile ApiExceptionRequestHandler apiExceptionHandler = new DefaultApiExceptionRequestHandler();

    public static /*@NonNull*/ ApiExceptionRequestHandler getApiExceptionHandler() {
        return apiExceptionHandler;
    }

    public static void resetBlockHandler() {
        ApiExceptionCallbackManager.apiExceptionHandler = new DefaultApiExceptionRequestHandler();
    }

    public static void setApiExceptionHandler(ApiExceptionRequestHandler apiExceptionHandler) {
        Assert.notNull(apiExceptionHandler, "apiExceptionHandler cannot be null");
        ApiExceptionCallbackManager.apiExceptionHandler = apiExceptionHandler;
    }

    private ApiExceptionCallbackManager() {
    }
}
