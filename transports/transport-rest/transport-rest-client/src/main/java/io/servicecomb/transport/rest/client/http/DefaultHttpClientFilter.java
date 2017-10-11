/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.transport.rest.client.http;

import java.util.Collection;

import javax.ws.rs.core.HttpHeaders;

import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.codec.produce.ProduceProcessor;
import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.common.rest.filter.HttpClientFilter;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.exception.CommonExceptionData;
import io.servicecomb.swagger.invocation.exception.ExceptionFactory;
import io.servicecomb.swagger.invocation.response.ResponseMeta;

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

  @Override
  public Response afterReceiveResponse(Invocation invocation, HttpServletResponseEx responseEx) {
    OperationMeta operationMeta = invocation.getOperationMeta();
    ResponseMeta responseMeta = operationMeta.findResponseMeta(responseEx.getStatus());
    RestOperationMeta swaggerRestOperation = operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
    ProduceProcessor produceProcessor = findProduceProcessor(swaggerRestOperation, responseEx);
    if (produceProcessor == null) {
      String msg =
          String.format("path %s, statusCode %d, reasonPhrase %s, response content-type %s is not supported",
              swaggerRestOperation.getAbsolutePath(),
              responseEx.getStatus(),
              responseEx.getStatusType().getReasonPhrase(),
              responseEx.getHeader(HttpHeaders.CONTENT_TYPE));
      Exception exception = ExceptionFactory.createConsumerException(new CommonExceptionData(msg));
      return Response.consumerFailResp(exception);
    }

    Object result = null;
    try {
      result = produceProcessor.decodeResponse(responseEx.getBodyBuffer(), responseMeta.getJavaType());
    } catch (Exception e) {
      return Response.consumerFailResp(e);
    }

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
