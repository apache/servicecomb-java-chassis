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

package io.servicecomb.common.rest.codec.param;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;

import io.servicecomb.common.rest.codec.RestClientRequest;
import io.servicecomb.common.rest.codec.RestObjectMapper;
import com.huawei.paas.foundation.vertx.stream.BufferOutputStream;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2017年1月23日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class RestClientRequestImpl implements RestClientRequest {
    protected HttpClientRequest request;

    protected Map<String, String> cookieMap;

    protected Map<String, Object> formMap;

    protected Buffer bodyBuffer;

    /**
     * <构造函数> [参数说明]
     */
    public RestClientRequestImpl(HttpClientRequest request) {
        this.request = request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(Buffer bodyBuffer) {
        this.bodyBuffer = bodyBuffer;
    }

    /**
     * {@inheritDoc}
     * @throws Exception
     */
    @Override
    public void end() throws Exception {
        writeCookies();

        genBodyBuffer();

        if (bodyBuffer == null) {
            request.end();
            return;
        }

        request.end(bodyBuffer);
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @throws Exception
     */
    private void genBodyBuffer() throws Exception {
        request.putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        if (bodyBuffer != null) {
            return;
        }

        if (formMap == null) {
            return;
        }

        request.putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        try (BufferOutputStream output = new BufferOutputStream()) {
            for (Entry<String, Object> entry : formMap.entrySet()) {
                output.write(entry.getKey().getBytes(StandardCharsets.UTF_8));
                output.write('=');
                if (entry.getValue() != null) {
                    String value = RestObjectMapper.INSTANCE.convertToString(entry.getValue());
                    output.write(value.getBytes(StandardCharsets.UTF_8));
                }
                output.write('&');
            }
            bodyBuffer = output.getBuffer();
        }
    }

    private void writeCookies() {
        if (cookieMap == null) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (Entry<String, String> entry : cookieMap.entrySet()) {
            builder.append(entry.getKey())
                    .append('=')
                    .append(entry.getValue())
                    .append("; ");
        }
        request.putHeader(HttpHeaders.COOKIE, builder.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCookie(String name, String value) {
        if (cookieMap == null) {
            cookieMap = new HashMap<>();
        }

        cookieMap.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addForm(String name, Object value) {
        if (formMap == null) {
            formMap = new HashMap<>();
        }

        formMap.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putHeader(String name, String value) {
        request.putHeader(name, value);
    }

}
