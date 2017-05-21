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

package io.servicecomb.common.rest.codec;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.servicecomb.common.rest.definition.RestOperationMeta;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import io.servicecomb.common.rest.codec.param.BodyProcessorCreator;
import io.servicecomb.common.rest.codec.param.CookieProcessorCreator;
import io.servicecomb.common.rest.codec.param.FormProcessorCreator;
import io.servicecomb.common.rest.codec.param.HeaderProcessorCreator;
import io.servicecomb.common.rest.codec.param.ParamValueProcessor;
import io.servicecomb.common.rest.codec.param.ParamValueProcessorCreator;
import io.servicecomb.common.rest.codec.param.ParamValueProcessorCreatorManager;
import io.servicecomb.common.rest.codec.param.PathProcessorCreator;
import io.servicecomb.common.rest.codec.param.QueryProcessorCreator;
import io.servicecomb.common.rest.codec.param.RestClientRequestImpl;
import io.servicecomb.foundation.common.utils.ReflectUtils;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import mockit.Mock;
import mockit.MockUp;

public class TestParam {

    private Map<String, String> serverPathParams = new HashMap<>();

    private Map<String, List<String>> serverQueryParams = new HashMap<>();

    private Map<String, List<String>> serverHttpHeaders = new HashMap<>();

    private RestServerRequest serverRequest =
        new LocalRestServerRequest(serverPathParams, serverQueryParams, serverHttpHeaders, null);

    private Map<String, List<String>> clientHttpHeaders = new HashMap<>();

    private Buffer clientBodyBuffer;

    private HttpClientRequest request;

    private RestClientRequest clientRequest = new RestClientRequestImpl(request);

    @Before
    public void setup() {
        request = new MockUp<HttpClientRequest>() {
            @Mock
            public HttpClientRequest putHeader(CharSequence name, CharSequence value) {
                this.putHeader(name.toString(), value.toString());

                return request;
            }

            @Mock
            public HttpClientRequest putHeader(String name, String value) {
                List<String> list = clientHttpHeaders.get(name);
                if (list == null) {
                    list = new ArrayList<>();
                    clientHttpHeaders.put(name, list);
                }
                list.add(value);

                return request;
            }

            @Mock
            public void end(Buffer chunk) {
                clientBodyBuffer = chunk;
            }
        }.getMockInstance();

        clientRequest = new RestClientRequestImpl(request);
    }

    @Test
    public void testCookie() throws Exception {
        ParamValueProcessorCreator creator =
            ParamValueProcessorCreatorManager.INSTANCE.findValue(CookieProcessorCreator.PARAMTYPE);
        ParamValueProcessor processor = creator.create("c1", String.class);

        Date date = new Date();
        processor.setValue(clientRequest, date);
        clientRequest.end();
        List<String> cookies = clientHttpHeaders.get(HttpHeaders.COOKIE.toString());
        Assert.assertEquals("c1=" + ISO8601Utils.format(date) + "; ", cookies.get(0));

        serverHttpHeaders.put(HttpHeaders.COOKIE.toString(), cookies);
        Object newDate = processor.getValue(serverRequest);
        Assert.assertEquals(ISO8601Utils.format(date), newDate);
    }

    @Test
    public void testPath() throws Exception {
        ParamValueProcessorCreator creator =
            ParamValueProcessorCreatorManager.INSTANCE.findValue(PathProcessorCreator.PARAMTYPE);
        ParamValueProcessor processor = creator.create("p1", String.class);

        serverPathParams.put("p1", "path");
        Object value = processor.getValue(serverRequest);
        Assert.assertEquals("path", value);
    }

    @Test
    public void testQuery() throws Exception {
        ParamValueProcessorCreator creator =
            ParamValueProcessorCreatorManager.INSTANCE.findValue(QueryProcessorCreator.PARAMTYPE);

        ParamValueProcessor processor = creator.create("q1", Date.class);
        Date value = (Date) processor.getValue(serverRequest);
        Assert.assertEquals(null, value);

        Date date = new Date();
        String strDate = ISO8601Utils.format(date);
        serverQueryParams.put("q1", Arrays.asList(strDate));

        value = (Date) processor.getValue(serverRequest);
        Assert.assertEquals(date.toString(), value.toString());

        processor = creator.create("q1", Date[].class);
        Date[] values = (Date[]) processor.getValue(serverRequest);
        Assert.assertEquals(1, values.length);
        Assert.assertEquals(date.toString(), values[0].toString());
    }

    @Test
    public void testHeader() throws Exception {
        ParamValueProcessorCreator creator =
            ParamValueProcessorCreatorManager.INSTANCE.findValue(HeaderProcessorCreator.PARAMTYPE);
        ParamValueProcessor processor = creator.create("h1", Date.class);

        Date date = new Date();
        processor.setValue(clientRequest, date);
        clientRequest.end();
        List<String> headValues = clientHttpHeaders.get("h1");
        Assert.assertEquals(ISO8601Utils.format(date), headValues.get(0));

        serverHttpHeaders.put("h1", headValues);
        Object newDate = processor.getValue(serverRequest);
        Assert.assertEquals(date.toString(), newDate.toString());
    }

    @Test
    public void testForm() throws Exception {
        ParamValueProcessorCreator creator =
            ParamValueProcessorCreatorManager.INSTANCE.findValue(FormProcessorCreator.PARAMTYPE);
        ParamValueProcessor processor = creator.create("f1", Date.class);

        Date date = new Date();
        processor.setValue(clientRequest, date);
        clientRequest.end();
        Assert.assertEquals("f1=" + ISO8601Utils.format(date) + "&", clientBodyBuffer.toString());
    }

    static class Body {
        public Date date = new Date();
    }

    @Test
    public void testBody() throws Exception {
        ParamValueProcessorCreator creator =
            ParamValueProcessorCreatorManager.INSTANCE.findValue(BodyProcessorCreator.PARAMTYPE);
        ParamValueProcessor processor = creator.create("", Body.class);

        Body body = new Body();
        processor.setValue(clientRequest, body);
        clientRequest.end();

        String expect = RestObjectMapper.INSTANCE.writeValueAsString(body);
        Assert.assertEquals(expect, clientBodyBuffer.toString());

        ByteArrayInputStream is = new ByteArrayInputStream(expect.getBytes(StandardCharsets.UTF_8));
        ReflectUtils.setField(serverRequest, "bodyObject", is);
        is.close();
        Body result = (Body) processor.getValue(serverRequest);
        Assert.assertEquals(body.date.toString(), result.date.toString());
    }

    @Test
    public void testRestCodec() {
        boolean status = false;
        try {
            RestCodec.argsToRest((new String[] {"test"}),
                    Mockito.mock(RestOperationMeta.class),
                    Mockito.mock(RestClientRequest.class));
            RestCodec.restToArgs(Mockito.mock(RestServerRequest.class), Mockito.mock(RestOperationMeta.class));
        } catch (Exception e) {
            status = true;
        }
        Assert.assertFalse(status);
    }

}
