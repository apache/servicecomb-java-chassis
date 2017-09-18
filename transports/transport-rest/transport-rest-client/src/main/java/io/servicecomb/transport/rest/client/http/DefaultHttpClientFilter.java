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

import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.codec.produce.ProduceProcessor;
import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.common.rest.filter.HttpClientFilter;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.exception.CommonExceptionData;
import io.servicecomb.swagger.invocation.exception.ExceptionFactory;
import io.servicecomb.swagger.invocation.response.ResponseMeta;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;

public class DefaultHttpClientFilter implements HttpClientFilter {

  @Override
  public int getOrder() {
    return Integer.MAX_VALUE;
  }

  @Override
  public void beforeSendRequest(Invocation invocation, HttpClientRequest clientRequest, Buffer bodyBuffer) {

  }

  protected ProduceProcessor findProduceProcessor(RestOperationMeta restOperation,
      HttpClientResponse httpResponse) {
    String contentType = httpResponse.getHeader(HttpHeaders.CONTENT_TYPE);
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
  public Response afterReceiveResponse(Invocation invocation, HttpClientResponse httpResponse, Buffer bodyBuffer)
      throws Exception {
    OperationMeta operationMeta = invocation.getOperationMeta();
    ResponseMeta responseMeta = operationMeta.findResponseMeta(httpResponse.statusCode());
    RestOperationMeta swaggerRestOperation = operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
    ProduceProcessor produceProcessor = findProduceProcessor(swaggerRestOperation, httpResponse);
    if (produceProcessor == null) {
      String msg =
          String.format("path %s, statusCode %d, reasonPhrase %s, response content-type %s is not supported",
              swaggerRestOperation.getAbsolutePath(),
              httpResponse.statusCode(),
              httpResponse.statusMessage(),
              httpResponse.getHeader(HttpHeaders.CONTENT_TYPE));
      Exception exception = ExceptionFactory.createConsumerException(new CommonExceptionData(msg));
      return Response.consumerFailResp(exception);
    }

    Object result = produceProcessor.decodeResponse(bodyBuffer, responseMeta.getJavaType());
    Response response =
        Response.create(httpResponse.statusCode(), httpResponse.statusMessage(), result);
    for (String headerName : responseMeta.getHeaders().keySet()) {
      List<String> headerValues = httpResponse.headers().getAll(headerName);
      for (String headerValue : headerValues) {
        response.getHeaders().addHeader(headerName, headerValue);
      }
    }

    return response;
  }
}
