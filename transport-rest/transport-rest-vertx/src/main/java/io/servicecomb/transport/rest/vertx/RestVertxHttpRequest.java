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

package io.servicecomb.transport.rest.vertx;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.servicecomb.common.rest.codec.RestServerRequestInternal;
import io.servicecomb.foundation.vertx.stream.BufferInputStream;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;

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

    @Override
    public String getPath() {
        return request.path();
    }

    @Override
    public String getMethod() {
        return request.method().name();
    }

    public void setPathParamMap(Map<String, String> pathParamMap) {
        this.pathParamMap = pathParamMap;
    }

    @Override
    public void complete() {
        future.complete();
    }

    @Override
    public String[] getQueryParam(String key) {
        List<String> paramList = request.params().getAll(key);
        if (paramList == null) {
            return null;
        }

        return (String[]) paramList.toArray(new String[paramList.size()]);
    }

    @Override
    public String getPathParam(String key) {
        return this.pathParamMap.get(key);
    }

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

    @Override
    public Object getFormParam(String key) {
        return context.request().getParam(key);
    }

    @Override
    public String getCookieParam(String key) {
        Cookie cookie = context.getCookie(key);
        if (cookie != null) {
            return cookie.getValue();
        }

        return null;
    }

    @Override
    public InputStream getBody() {
        return new BufferInputStream(context.getBody().getByteBuf());
    }

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
