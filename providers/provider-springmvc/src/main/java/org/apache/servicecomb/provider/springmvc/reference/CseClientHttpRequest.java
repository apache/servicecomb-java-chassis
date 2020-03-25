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

package org.apache.servicecomb.provider.springmvc.reference;

import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.RestCodec;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.locator.OperationLocator;
import org.apache.servicecomb.common.rest.locator.ServicePathManager;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.core.provider.consumer.MicroserviceReferenceConfig;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.response.ResponsesMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import com.fasterxml.jackson.databind.type.TypeFactory;

import io.netty.handler.codec.http.QueryStringDecoder;

public class CseClientHttpRequest implements ClientHttpRequest {
  private static final Logger LOGGER = LoggerFactory.getLogger(CseClientHttpRequest.class);

  // URL format：cse://microserviceName/business url
  private URI uri;

  // business url
  private String path;

  private HttpMethod method;

  private HttpHeaders httpHeaders = new HttpHeaders();

  private InvocationContext context;

  private Object requestBody;

  private Map<String, List<String>> queryParams;

  private RequestMeta requestMeta;

  private Type responseType;

  public CseClientHttpRequest() {
  }

  public CseClientHttpRequest(URI uri, HttpMethod method) {
    this.uri = uri;
    this.method = method;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  protected RequestMeta getRequestMeta() {
    return requestMeta;
  }

  protected void setRequestMeta(RequestMeta requestMeta) {
    this.requestMeta = requestMeta;
  }

  public void setUri(URI uri) {
    this.uri = uri;
  }

  public void setMethod(HttpMethod method) {
    this.method = method;
  }

  protected void setQueryParams(Map<String, List<String>> queryParams) {
    this.queryParams = queryParams;
  }

  public InvocationContext getContext() {
    return context;
  }

  public void setContext(InvocationContext context) {
    this.context = context;
  }

  public Type getResponseType() {
    return responseType;
  }

  public void setResponseType(Type responseType) {
    this.responseType = responseType;
  }

  public void setRequestBody(Object requestBody) {
    this.requestBody = requestBody;
  }

  public void setHttpHeaders(HttpHeaders headers) {
    if (headers != null) {
      this.httpHeaders = headers;
    }
  }

  @Override
  public HttpMethod getMethod() {
    return method;
  }

  @Override
  public String getMethodValue() {
    return method.name();
  }

  @Override
  public URI getURI() {
    return uri;
  }

  @Override
  public HttpHeaders getHeaders() {
    return httpHeaders;
  }

  @Override
  public OutputStream getBody() {
    return null;
  }

  @Override
  public ClientHttpResponse execute() {
    path = findUriPath(uri);
    requestMeta = createRequestMeta(method.name(), uri);

    QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri.getRawSchemeSpecificPart());
    queryParams = queryStringDecoder.parameters();

    Map<String, Object> swaggerArguments = this.collectArguments();

    // 异常流程，直接抛异常出去
    return this.invoke(swaggerArguments);
  }

  protected RequestMeta createRequestMeta(String httpMethod, URI uri) {
    String microserviceName = uri.getAuthority();

    MicroserviceReferenceConfig microserviceReferenceConfig = SCBEngine.getInstance()
        .createMicroserviceReferenceConfig(microserviceName);
    MicroserviceMeta microserviceMeta = microserviceReferenceConfig.getLatestMicroserviceMeta();

    ServicePathManager servicePathManager = ServicePathManager.getServicePathManager(microserviceMeta);
    if (servicePathManager == null) {
      throw new Error(String.format("no schema defined for %s:%s",
          microserviceMeta.getAppId(),
          microserviceMeta.getMicroserviceName()));
    }

    OperationLocator locator = servicePathManager.consumerLocateOperation(path, httpMethod);
    RestOperationMeta swaggerRestOperation = locator.getOperation();

    OperationMeta operationMeta = locator.getOperation().getOperationMeta();
    ReferenceConfig referenceConfig = microserviceReferenceConfig.createReferenceConfig(operationMeta);

    Map<String, String> pathParams = locator.getPathVarMap();
    return new RequestMeta(referenceConfig, swaggerRestOperation, pathParams);
  }

  protected String findUriPath(URI uri) {
    return uri.getRawPath();
  }

  protected Invocation prepareInvocation(Map<String, Object> swaggerArguments) {
    Invocation invocation =
        InvocationFactory.forConsumer(requestMeta.getReferenceConfig(),
            requestMeta.getOperationMeta(),
            swaggerArguments);

    invocation.setWeakInvoke(true);
    invocation.getHandlerContext().put(RestConst.REST_CLIENT_REQUEST_PATH,
        path + (this.uri.getRawQuery() == null ? "" : "?" + this.uri.getRawQuery()));

    if (context != null) {
      invocation.addContext(context.getContext());
      invocation.addLocalContext(context.getLocalContext());
    }

    if (responseType != null &&
        !(responseType instanceof Class && Part.class.isAssignableFrom((Class<?>) responseType))) {
      ResponsesMeta responsesMeta = new ResponsesMeta();
      invocation.getOperationMeta().getResponsesMeta().cloneTo(responsesMeta);
      responsesMeta.getResponseMap().put(Status.OK.getStatusCode(),
          TypeFactory.defaultInstance().constructType(responseType));
      invocation.setResponsesMeta(responsesMeta);
    }

    invocation.getHandlerContext().put(RestConst.CONSUMER_HEADER, httpHeaders);
    return invocation;
  }

  private CseClientHttpResponse invoke(Map<String, Object> swaggerArguments) {
    Invocation invocation = prepareInvocation(swaggerArguments);
    Response response = doInvoke(invocation);

    if (response.isSuccessed()) {
      return new CseClientHttpResponse(response);
    }

    throw ExceptionFactory.convertConsumerException(response.getResult());
  }

  protected Response doInvoke(Invocation invocation) {
    return InvokerUtils.innerSyncInvoke(invocation);
  }

  protected Map<String, Object> collectArguments() {
    HttpServletRequest mockRequest = new CommonToHttpServletRequest(requestMeta.getPathParams(), queryParams,
        httpHeaders, requestBody, requestMeta.getSwaggerRestOperation().isFormData(),
        requestMeta.getSwaggerRestOperation().getFileKeys());
    // no types info, so will not convert any parameters
    return RestCodec.restToArgs(mockRequest, requestMeta.getSwaggerRestOperation());
  }
}
