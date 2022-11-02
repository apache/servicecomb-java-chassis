/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.common.rest.codec.param;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.common.rest.codec.param.BodyProcessorCreator.BodyProcessor;
import org.apache.servicecomb.common.rest.codec.param.BodyProcessorCreator.RawJsonBodyProcessor;
import org.apache.servicecomb.foundation.vertx.stream.BufferInputStream;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.type.TypeFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.http.impl.headers.HeadersMultiMap;


public class TestBodyProcessor {

  final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

  MultiMap headers;

  final RestClientRequest clientRequest = Mockito.mock(RestClientRequest.class);

  ParamValueProcessor processor;

  final ByteBuf inputBodyByteBuf = Unpooled.buffer();

  final BufferInputStream inputStream = new BufferInputStream(inputBodyByteBuf);

  Buffer outputBodyBuffer;

  String value;

  private void createProcessor(Class<?> type) {
    processor = new BodyProcessor(TypeFactory.defaultInstance().constructType(type), type.equals(String.class), true);
  }

  private void createRawJsonProcessor() {
    processor = new RawJsonBodyProcessor(TypeFactory.defaultInstance().constructType(String.class), true, true);
  }

  private void createClientRequest() {
    Mockito.doAnswer(invocation -> {
      headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
      return null;
    }).when(clientRequest).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

    Mockito.when(clientRequest.getHeaders()).thenReturn(headers);
  }

  private void initInputStream() throws IOException {
    Mockito.when(request.getInputStream()).thenReturn(inputStream);
  }

  private void setupGetValue(Class<?> type) throws IOException {
    createProcessor(type);
    initInputStream();
  }

  @BeforeEach
  public void before() {
    headers = new HeadersMultiMap();
    value = "value";
  }

  @Test
  public void testGetValueHaveAttr() throws Exception {
    int body = 10;
    createProcessor(String.class);
    Mockito.when(request.getAttribute(RestConst.BODY_PARAMETER)).thenReturn(body);

    Object result = processor.getValue(request);
    Assertions.assertEquals("10", result);
  }

  @Test
  public void testGetValueNoAttrNoStream() throws Exception {
    createProcessor(String.class);
    Mockito.when(request.getInputStream()).thenReturn(null);
    Assertions.assertThrows(InvocationException.class, () -> processor.getValue(request));
  }

  @Test
  public void testGetValueTextPlain() throws Exception {
    setupGetValue(String.class);
    inputBodyByteBuf.writeCharSequence("abc", StandardCharsets.UTF_8);

    Mockito.when(request.getContentType()).thenReturn(MediaType.TEXT_PLAIN);

    Assertions.assertEquals("abc", processor.getValue(request));
  }

  @Test
  public void testGetValueContextTypeJson() throws Exception {
    setupGetValue(Integer.class);
    inputBodyByteBuf.writeCharSequence("\"1\"", StandardCharsets.UTF_8);

    Mockito.when(request.getContentType()).thenReturn(MediaType.APPLICATION_JSON);

    Assertions.assertEquals(1, processor.getValue(request));
  }

  @Test
  public void testGetValueDefaultJson() throws Exception {
    setupGetValue(Integer.class);
    inputBodyByteBuf.writeCharSequence("\"1\"", StandardCharsets.UTF_8);

    Assertions.assertEquals(1, processor.getValue(request));
  }

  @Test
  public void testSetValue() throws Exception {
    createClientRequest();
    createProcessor(String.class);

    Mockito.when(clientRequest.getHeaders()).thenReturn(headers);
    ParamValueProcessor spy = Mockito.spy(processor);
    Mockito.doAnswer(invocation -> {
      outputBodyBuffer = new BufferImpl().appendBytes((value).getBytes(StandardCharsets.UTF_8));
      return null;
    }).when(spy).setValue(clientRequest, value);

    spy.setValue(clientRequest, value);
    processor.setValue(clientRequest, value);
    Assertions.assertEquals(MediaType.APPLICATION_JSON, headers.get(HttpHeaders.CONTENT_TYPE));
    Assertions.assertEquals(value, outputBodyBuffer.toString());
  }

  @Test
  public void testSetValueTextPlain() throws Exception {
    createClientRequest();
    createProcessor(String.class);
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);

    ParamValueProcessor spy = Mockito.spy(processor);
    Mockito.doAnswer(invocation -> {
      outputBodyBuffer = new BufferImpl().appendBytes((value).getBytes(StandardCharsets.UTF_8));
      return null;
    }).when(spy).setValue(clientRequest, value);

    spy.setValue(clientRequest, value);
    processor.setValue(clientRequest, value);
    Assertions.assertEquals(MediaType.TEXT_PLAIN, headers.get(HttpHeaders.CONTENT_TYPE));
    Assertions.assertEquals(value, outputBodyBuffer.toString());
  }

  @Test
  public void testSetValueTextPlainTypeMismatch() {
    createClientRequest();
    createProcessor(String.class);
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);

    try {
      processor.setValue(clientRequest, new Date());
      Assertions.fail("an exception is expected!");
    } catch (Exception e) {
      Assertions.assertEquals(IllegalArgumentException.class, e.getClass());
      Assertions.assertEquals("Content-Type is text/plain while arg type is not String", e.getMessage());
    }
  }

  @Test
  public void testGetParameterPath() {
    createProcessor(String.class);
    Assertions.assertEquals("", processor.getParameterPath());
  }

  @Test
  public void testGetProcessorType() {
    createProcessor(String.class);
    Assertions.assertEquals("body", processor.getProcessorType());
  }

  @Test
  public void testGetValueRawJson() throws Exception {
    createRawJsonProcessor();
    initInputStream();
    inputBodyByteBuf.writeCharSequence("\"1\"", StandardCharsets.UTF_8);

    Assertions.assertEquals("\"1\"", processor.getValue(request));
  }

  @Test
  public void testGetValueRawJsonHaveAttr() throws Exception {
    int body = 10;
    createRawJsonProcessor();
    Mockito.when(request.getAttribute(RestConst.BODY_PARAMETER)).thenReturn(body);

    Object result = processor.getValue(request);
    Assertions.assertEquals("10", result);
  }

  @Test
  public void testGetValueRawJsonNoAttrNoStream() throws Exception {
    createRawJsonProcessor();
    Mockito.when(request.getInputStream()).thenReturn(null);

    Object result = processor.getValue(request);
    Assertions.assertNull(result);
  }

  @Test
  public void testSetValueRawJson() throws Exception {
    createClientRequest();
    createRawJsonProcessor();

    ParamValueProcessor spy = Mockito.spy(processor);
    Mockito.doAnswer(invocation -> {
      outputBodyBuffer = new BufferImpl().appendBytes((value).getBytes(StandardCharsets.UTF_8));
      return null;
    }).when(spy).setValue(clientRequest, value);
    spy.setValue(clientRequest, value);
    processor.setValue(clientRequest, value);
    Assertions.assertEquals(MediaType.APPLICATION_JSON, headers.get(HttpHeaders.CONTENT_TYPE));
    Assertions.assertEquals("value", outputBodyBuffer.toString());
  }

  static class BodyModel {
    public String name;

    public int age;
  }

  @Test
  public void convertFromFormData() throws Exception {
    createProcessor(BodyModel.class);
    Map<String, String[]> parameterMap = new HashMap<>();
    parameterMap.put("name", new String[] {"n"});
    parameterMap.put("age", new String[] {"10"});
    Mockito.when(request.getParameterMap()).thenReturn(parameterMap);
    Mockito.when(request.getContentType()).thenReturn(MediaType.MULTIPART_FORM_DATA + ";utf-8");

    BodyModel bm = (BodyModel) processor.getValue(request);
    Assertions.assertEquals("n", bm.name);
    Assertions.assertEquals(10, bm.age);
  }

  @Test
  public void convertFromUrlencoded() throws Exception {
    createProcessor(BodyModel.class);
    Map<String, String[]> parameterMap = new HashMap<>();
    parameterMap.put("name", new String[] {"n"});
    parameterMap.put("age", new String[] {"10"});
    Mockito.when(request.getParameterMap()).thenReturn(parameterMap);
    Mockito.when(request.getContentType()).thenReturn(MediaType.APPLICATION_FORM_URLENCODED + ";utf-8");

    BodyModel bm = (BodyModel) processor.getValue(request);
    Assertions.assertEquals("n", bm.name);
    Assertions.assertEquals(10, bm.age);
  }
}
