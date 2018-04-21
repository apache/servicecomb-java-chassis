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

import java.util.Collection;

import javax.ws.rs.core.HttpHeaders;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessor;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.filter.HttpClientFilter;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.response.ResponseMeta;

public class DefaultHttpClientFilter implements HttpClientFilter {

  @Override
  public int getOrder() {
    return Integer.MAX_VALUE;
  }

  @Override
  public void beforeSendRequest(Invocation invocation, HttpServletRequestEx requestEx) {

  }

  protected ProduceProcessor findProduceProcessor(RestOperationMeta restOperation,
      HttpServletResponseEx responseEx) {
    String contentType = responseEx.getHeader(HttpHeaders.CONTENT_TYPE);
    if (contentType == null) {
      return null;
    }

    String contentTypeForFind = contentType;
    int idx = contentType.indexOf(';');
    if (idx != -1) {
      contentTypeForFind = contentType.substring(0, idx);
    }
    return restOperation.findProduceProcessor(contentTypeForFind);
  }

  protected Object extractResult(Invocation invocation, HttpServletResponseEx responseEx) {
    Object result = invocation.getHandlerContext().get(RestConst.READ_STREAM_PART);
    if (result != null) {
      return result;
    }

    OperationMeta operationMeta = invocation.getOperationMeta();
    ResponseMeta responseMeta = operationMeta.findResponseMeta(responseEx.getStatus());
    RestOperationMeta swaggerRestOperation = operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
    ProduceProcessor produceProcessor = findProduceProcessor(swaggerRestOperation, responseEx);
    if (produceProcessor == null) {
      String msg =
          String.format("method %s, path %s, statusCode %d, reasonPhrase %s, response content-type %s is not supported",
              swaggerRestOperation.getHttpMethod(),
              swaggerRestOperation.getAbsolutePath(),
              responseEx.getStatus(),
              responseEx.getStatusType().getReasonPhrase(),
              responseEx.getHeader(HttpHeaders.CONTENT_TYPE));
      return ExceptionFactory.createConsumerException(new CommonExceptionData(msg));
    }

    try {
      return produceProcessor.decodeResponse(responseEx.getBodyBuffer(), responseMeta.getJavaType());
    } catch (Exception e) {
      return ExceptionFactory.createConsumerException(e);
    }
  }

  @Override
  public Response afterReceiveResponse(Invocation invocation, HttpServletResponseEx responseEx) {
    Object result = extractResult(invocation, responseEx);

    Response response =
        Response.create(responseEx.getStatusType(), result);
    for (String headerName : responseEx.getHeaderNames()) {
      Collection<String> headerValues = responseEx.getHeaders(headerName);
      for (String headerValue : headerValues) {
        response.getHeaders().addHeader(headerName, headerValue);
      }
    }

    return response;
  }
}
