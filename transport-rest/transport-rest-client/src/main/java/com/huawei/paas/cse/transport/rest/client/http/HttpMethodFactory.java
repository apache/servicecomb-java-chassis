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

package com.huawei.paas.cse.transport.rest.client.http;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.HttpMethod;

/**
 * 返回各种Http Method的实例
 * @author   
 * @version  [版本号, 2017年1月12日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public final class HttpMethodFactory {

    private static Map<String, VertxHttpMethod> httpMethodMap = new HashMap<>();

    static {
        addHttpMethod(HttpMethod.GET, VertxGetMethod.INSTANCE);
        addHttpMethod(HttpMethod.POST, VertxPostMethod.INSTANCE);
        addHttpMethod(HttpMethod.PUT, VertxPutMethod.INSTANCE);
        addHttpMethod(HttpMethod.DELETE, VertxDeleteMethod.INSTANCE);
    }

    static void addHttpMethod(String httpMethod, VertxHttpMethod instance) {
        httpMethodMap.put(httpMethod, instance);
    }

    private HttpMethodFactory() {
    }

    public static VertxHttpMethod findHttpMethodInstance(String method) throws Exception {
        VertxHttpMethod httpMethod = httpMethodMap.get(method);
        if (httpMethod == null) {
            throw new Exception(String.format("Http method %s is not supported", method));
        }

        return httpMethod;
    }
}
