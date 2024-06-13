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
package cn.futuai.open.encrypt.callback;

import cn.hutool.core.lang.Assert;

/**
 * 网关加密回调管理器
 * @author Jason Kung
 * @date 2024/6/7 14:15
 */
public final class GatewayApiInvalidCallbackManager {

    /**
     * ApiRequestExceptionHandler: (serverExchange, exception) -> response
     */
    private static volatile ApiValidExceptionRequestHandler apiInvalidHandler = new DefaultApiValidExceptionRequestHandler();

    public static /*@NonNull*/ ApiValidExceptionRequestHandler getApiInvalidHandler() {
        return apiInvalidHandler;
    }

    public static void resetBlockHandler() {
        GatewayApiInvalidCallbackManager.apiInvalidHandler = new DefaultApiValidExceptionRequestHandler();
    }

    public static void setApiInvalidHandler(ApiValidExceptionRequestHandler apiInvalidHandler) {
        Assert.notNull(apiInvalidHandler, "apiInvalidHandler cannot be null");
        GatewayApiInvalidCallbackManager.apiInvalidHandler = apiInvalidHandler;
    }

    private GatewayApiInvalidCallbackManager() {
    }
}
