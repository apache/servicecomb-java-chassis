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

package com.huawei.paas.cse.transport.rest.vertx;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.huawei.paas.cse.common.rest.codec.RestServerRequestInternal;
import com.huawei.paas.foundation.vertx.stream.BufferInputStream;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;

/**
 * 将HttpServerRequest封装为RestHttpRequest接口的类型，统一多种rest transport
 * @author   
 * @version  [版本号, 2017年1月2日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class RestVertxHttpRequest implements RestServerRequestInternal {
    private RoutingContext context;

    private HttpServerRequest request;

    private Future<Object> future;

    private Map<String, String> pathParamMap;

    public RestVertxHttpRequest(RoutingContext context, Future<Object> future) {
        this.context = context;
        this.request = context.request();
        this.future = future;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        return request.path();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMethod() {
        return request.method().name();
    }

    /**
     * 对pathParamMap进行赋值
     * @param pathParamMap pathParamMap的新值
     */
    public void setPathParamMap(Map<String, String> pathParamMap) {
        this.pathParamMap = pathParamMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void complete() {
        future.complete();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getQueryParam(String key) {
        List<String> paramList = request.params().getAll(key);
        if (paramList == null) {
            return null;
        }

        return (String[]) paramList.toArray(new String[paramList.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPathParam(String key) {
        return this.pathParamMap.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHeaderParam(String key) {
        Iterator<Entry<String, String>> ite = request.headers().iterator();
        while (ite.hasNext()) {
            Entry<String, String> entry = ite.next();
            if (key.equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getFormParam(String key) {
        return context.request().getParam(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCookieParam(String key) {
        Cookie cookie = context.getCookie(key);
        if (cookie != null) {
            return cookie.getValue();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getBody() {
        return new BufferInputStream(context.getBody().getByteBuf());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String[]> getQueryParams() {
        Map<String, String[]> queryMap = new HashMap<>();

        for (String name : request.params().names()) {
            List<String> param = request.params().getAll(name);
            queryMap.put(name, param.toArray(new String[param.size()]));
        }

        return queryMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getHttpRequest() {
        return (T) request;
    }

}
