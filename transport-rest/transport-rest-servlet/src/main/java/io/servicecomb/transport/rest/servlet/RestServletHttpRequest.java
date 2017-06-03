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

package io.servicecomb.transport.rest.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import io.servicecomb.common.rest.codec.RestServerRequestInternal;

public class RestServletHttpRequest implements RestServerRequestInternal {
    private HttpServletRequest request;

    private AsyncContext asyncCtx;

    private Map<String, String> pathParamMap;

    public RestServletHttpRequest(HttpServletRequest request, AsyncContext asyncCtx) {
        this.request = request;
        this.asyncCtx = asyncCtx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        return request.getRequestURI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMethod() {
        return request.getMethod();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPathParamMap(Map<String, String> pathParamMap) {
        this.pathParamMap = pathParamMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void complete() {
        asyncCtx.complete();
    }

    @Override
    public String[] getQueryParam(String key) {
        return request.getParameterMap().get(key);
    }

    @Override
    public String getPathParam(String key) {
        return this.pathParamMap.get(key);
    }

    @Override
    public String getHeaderParam(String key) {
        return request.getHeader(key);
    }

    @Override
    public Object getFormParam(String key) {
        return request.getParameter(key);
    }

    @Override
    public String getCookieParam(String key) {
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(key)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    public InputStream getBody() throws IOException {
        return request.getInputStream();
    }

    @Override
    public Map<String, String[]> getQueryParams() {
        return request.getParameterMap();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getHttpRequest() {
        return (T) request;
    }

}
