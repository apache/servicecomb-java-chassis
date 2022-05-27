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

package org.apache.servicecomb.transport.rest.client.http;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.produce.ProduceJsonProcessor;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessor;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.foundation.vertx.http.ReadStreamPart;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

public class TestDefaultHttpClientFilter {
  private final DefaultHttpClientFilter filter = new DefaultHttpClientFilter();

  @Test
  public void testOrder() {
    Assertions.assertEquals(10000, filter.getOrder());
  }

  @Test
  public void testFindProduceProcessorNullContentType(@Mocked RestOperationMeta restOperation,
      @Mocked HttpServletResponseEx responseEx) {
    new Expectations() {
      {
        responseEx.getHeader(HttpHeaders.CONTENT_TYPE);
        result = null;
      }
    };

    Assertions.assertNull(filter.findProduceProcessor(restOperation, responseEx));
  }

  @Test
  public void testFindProduceProcessorJson(@Mocked RestOperationMeta restOperation,
      @Mocked HttpServletResponseEx responseEx, @Mocked ProduceProcessor produceProcessor) {
    new Expectations() {
      {
        responseEx.getHeader(HttpHeaders.CONTENT_TYPE);
        result = "json";
        restOperation.findProduceProcessor("json");
        result = produceProcessor;
      }
    };

    Assertions.assertSame(produceProcessor, filter.findProduceProcessor(restOperation, responseEx));
  }

  @Test
  public void testFindProduceProcessorJsonWithCharset(@Mocked RestOperationMeta restOperation,
      @Mocked HttpServletResponseEx responseEx, @Mocked ProduceProcessor produceProcessor) {
    new Expectations() {
      {
        responseEx.getHeader(HttpHeaders.CONTENT_TYPE);
        result = "json; UTF-8";
        restOperation.findProduceProcessor("json");
        result = produceProcessor;
      }
    };

    Assertions.assertSame(produceProcessor, filter.findProduceProcessor(restOperation, responseEx));
  }

  @Test
  public void extractResult_readStreamPart(@Mocked Invocation invocation, @Mocked ReadStreamPart part,
      @Mocked HttpServletResponseEx responseEx) {
    Map<String, Object> handlerContext = new HashMap<>();
    handlerContext.put(RestConst.READ_STREAM_PART, part);
    new Expectations() {
      {
        invocation.getHandlerContext();
        result = handlerContext;
        responseEx.getStatusType();
        result = Status.OK;
      }
    };

    Response response = filter.extractResponse(invocation, responseEx);
    Assertions.assertSame(part, response.getResult());
    Assertions.assertEquals(Status.OK, response.getStatus());
  }

  @Test
  public void extractResult_decodeError(@Mocked Invocation invocation, @Mocked ReadStreamPart part,
      @Mocked OperationMeta operationMeta,
      @Mocked RestOperationMeta swaggerRestOperation,
      @Mocked HttpServletResponseEx responseEx) {
    Map<String, Object> handlerContext = new HashMap<>();
    new Expectations() {
      {
        invocation.getHandlerContext();
        result = handlerContext;
        invocation.getOperationMeta();
        result = operationMeta;
        invocation.findResponseType(400);
        result = SimpleType.constructUnsafe(Date.class);
        operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
        result = swaggerRestOperation;
        responseEx.getStatus();
        result = 400;
        responseEx.getBodyBuffer();
        result = new BufferImpl().appendString("abc");
      }
    };
    new MockUp<DefaultHttpClientFilter>() {
      @Mock
      ProduceProcessor findProduceProcessor(RestOperationMeta restOperation, HttpServletResponseEx responseEx) {
        return new ProduceJsonProcessor();
      }
    };

    Response response = filter.extractResponse(invocation, responseEx);
    Assertions.assertEquals(400, response.getStatusCode());
    Assertions.assertEquals(InvocationException.class, response.<InvocationException>getResult().getClass());
    InvocationException invocationException = response.getResult();
    Assertions.assertEquals(
        "InvocationException: code=400;msg=CommonExceptionData [message=method null, path null, statusCode 400, reasonPhrase null, response content-type null is not supported]",
        invocationException.getMessage());
    Assertions.assertEquals(
        "Unrecognized token 'abc': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n"
            + " at [Source: (org.apache.servicecomb.foundation.vertx.stream.BufferInputStream); line: 1, column: 4]",
        invocationException.getCause().getMessage());
    Assertions.assertEquals(CommonExceptionData.class, invocationException.getErrorData().getClass());
    CommonExceptionData commonExceptionData = (CommonExceptionData) invocationException.getErrorData();
    Assertions.assertEquals(
        "method null, path null, statusCode 400, reasonPhrase null, response content-type null is not supported",
        commonExceptionData.getMessage());
  }

  @Test
  public void extractResult_decodeError200(@Mocked Invocation invocation, @Mocked ReadStreamPart part,
      @Mocked OperationMeta operationMeta,
      @Mocked RestOperationMeta swaggerRestOperation,
      @Mocked HttpServletResponseEx responseEx) {
    Map<String, Object> handlerContext = new HashMap<>();
    new Expectations() {
      {
        invocation.getHandlerContext();
        result = handlerContext;
        invocation.getOperationMeta();
        result = operationMeta;
        invocation.findResponseType(200);
        result = SimpleType.constructUnsafe(Date.class);
        operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
        result = swaggerRestOperation;
        responseEx.getStatus();
        result = 200;
        responseEx.getBodyBuffer();
        result = new BufferImpl().appendString("abc");
      }
    };
    new MockUp<DefaultHttpClientFilter>() {
      @Mock
      ProduceProcessor findProduceProcessor(RestOperationMeta restOperation, HttpServletResponseEx responseEx) {
        return new ProduceJsonProcessor();
      }
    };

    Response response = filter.extractResponse(invocation, responseEx);
    Assertions.assertEquals(400, response.getStatusCode());
    Assertions.assertEquals(InvocationException.class, response.<InvocationException>getResult().getClass());
    InvocationException invocationException = response.getResult();
    Assertions.assertEquals(
        "InvocationException: code=400;msg=CommonExceptionData [message=method null, path null, statusCode 200, reasonPhrase null, response content-type null is not supported]",
        invocationException.getMessage());
    Assertions.assertEquals(
        "Unrecognized token 'abc': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n"
            + " at [Source: (org.apache.servicecomb.foundation.vertx.stream.BufferInputStream); line: 1, column: 4]",
        invocationException.getCause().getMessage());
    Assertions.assertEquals(CommonExceptionData.class, invocationException.getErrorData().getClass());
    CommonExceptionData commonExceptionData = (CommonExceptionData) invocationException.getErrorData();
    Assertions.assertEquals(
        "method null, path null, statusCode 200, reasonPhrase null, response content-type null is not supported",
        commonExceptionData.getMessage());
  }

  @Test
  public void testAfterReceiveResponseNullProduceProcessor(@Mocked Invocation invocation,
      @Mocked HttpServletResponseEx responseEx,
      @Mocked OperationMeta operationMeta,
      @Mocked RestOperationMeta swaggerRestOperation) throws Exception {
    CommonExceptionData data = new CommonExceptionData("abcd");
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
        result = swaggerRestOperation;
        invocation.findResponseType(403);
        result = SimpleType.constructUnsafe(CommonExceptionData.class);
        responseEx.getStatus();
        result = 403;
        responseEx.getStatusType();
        result = Status.FORBIDDEN;
        responseEx.getBodyBuffer();
        result = Buffer.buffer(JsonUtils.writeValueAsString(data).getBytes());
      }
    };

    Response response = filter.afterReceiveResponse(invocation, responseEx);
    Assertions.assertEquals(403, response.getStatusCode());
    Assertions.assertEquals("Forbidden", response.getReasonPhrase());
    Assertions.assertEquals(InvocationException.class, response.<InvocationException>getResult().getClass());
    InvocationException invocationException = response.getResult();
    Assertions.assertEquals(
        403,
        invocationException.getStatusCode());
    Assertions.assertEquals(
        "CommonExceptionData [message=abcd]",
        invocationException.getErrorData().toString());
  }

  @Test
  public void testAfterReceiveResponseNormal(@Mocked Invocation invocation,
      @Mocked HttpServletResponseEx responseEx,
      @Mocked Buffer bodyBuffer,
      @Mocked OperationMeta operationMeta,
      @Mocked RestOperationMeta swaggerRestOperation,
      @Mocked ProduceProcessor produceProcessor) throws Exception {
    MultiMap responseHeader = MultiMap.caseInsensitiveMultiMap();
    responseHeader.add("b", "bValue");

    Object decodedResult = new Object();
    new Expectations() {
      {
        responseEx.getHeader(HttpHeaders.CONTENT_TYPE);
        result = "json";
        responseEx.getHeaderNames();
        result = Arrays.asList("a", "b");
        responseEx.getHeaders("b");
        result = responseHeader.getAll("b");
        swaggerRestOperation.findProduceProcessor("json");
        result = produceProcessor;
        produceProcessor.decodeResponse(bodyBuffer, (JavaType) any);
        result = decodedResult;

        invocation.getOperationMeta();
        result = operationMeta;
        operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
        result = swaggerRestOperation;

        responseEx.getStatusType();
        result = Status.OK;
      }
    };

    Response response = filter.afterReceiveResponse(invocation, responseEx);
    Assertions.assertSame(decodedResult, response.getResult());
    Assertions.assertEquals(1, response.getHeaders().size());
    Assertions.assertEquals(response.getHeaders("b"), Arrays.asList("bValue"));
  }
}
