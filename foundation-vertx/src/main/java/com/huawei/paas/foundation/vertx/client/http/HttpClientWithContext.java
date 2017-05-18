/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.paas.foundation.vertx.client.http;

import io.vertx.core.Context;
import io.vertx.core.http.HttpClient;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2016年12月28日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class HttpClientWithContext {
    public interface RunHandler {
        void run(HttpClient httpClient);
    }

    private HttpClient httpClient;

    private Context context;

    /**
     * <构造函数> [参数说明]
     */
    public HttpClientWithContext(HttpClient httpClient, Context context) {
        this.httpClient = httpClient;
        this.context = context;
    }

    /**
     * 获取httpClient的值
     * @return 返回 httpClient
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void runOnContext(RunHandler handler) {
        context.runOnContext((v) -> {
            handler.run(httpClient);
        });
    }
}
