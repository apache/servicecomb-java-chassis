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

package io.servicecomb.provider.springmvc.reference;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.codec.RestCodec;
import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.common.rest.locator.OperationLocator;
import io.servicecomb.common.rest.locator.ServicePathManager;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.invocation.InvocationFactory;
import io.servicecomb.core.provider.consumer.InvokerUtils;
import io.servicecomb.core.provider.consumer.ReferenceConfig;
import io.servicecomb.core.provider.consumer.ReferenceConfigUtils;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.context.InvocationContext;
import io.servicecomb.swagger.invocation.exception.ExceptionFactory;

public class CseClientHttpRequest implements ClientHttpRequest {
  private URI uri;

  private HttpMethod method;

  private HttpHeaders httpHeaders = new HttpHeaders();

  private InvocationContext context;

  private Object requestBody;

  private Map<String, List<String>> queryParams;

  private RequestMeta requestMeta;

  public CseClientHttpRequest() {
  }

  public CseClientHttpRequest(URI uri, HttpMethod method) {
    this.uri = uri;
    this.method = method;
  }

  public InvocationContext getContext() {
    return context;
  }

  public void setContext(InvocationContext context) {
    this.context = context;
  }

  public void setRequestBody(Object requestBody) {
    this.requestBody = requestBody;
  }

  @Override
  public HttpMethod getMethod() {
    return method;
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
  public OutputStream getBody() throws IOException {
    return null;
  }

  @Override
  public ClientHttpResponse execute() throws IOException {
    requestMeta = createRequestMeta(method.name(), uri);

    QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri.getRawSchemeSpecificPart());
    queryParams = queryStringDecoder.parameters();

    Object[] args = this.collectArguments();

    // 异常流程，直接抛异常出去
    return this.invoke(args);
  }

  /**
   * 处理调用URL
   * URL格式：cse://microserviceName/业务url
   */
  private RequestMeta createRequestMeta(String httpMetod, URI uri) {
    String microserviceName = uri.getAuthority();
    ReferenceConfig referenceConfig = ReferenceConfigUtils.getForInvoke(microserviceName);

    MicroserviceMeta microserviceMeta = referenceConfig.getMicroserviceMeta();
    ServicePathManager servicePathManager = ServicePathManager.getServicePathManager(microserviceMeta);
    if (servicePathManager == null) {
      throw new Error(String.format("no schema defined for %s:%s",
          microserviceMeta.getAppId(),
          microserviceMeta.getName()));
    }

    OperationLocator locator = servicePathManager.consumerLocateOperation(uri.getPath(), httpMetod);
    RestOperationMeta swaggerRestOperation = locator.getOperation();

    Map<String, String> pathParams = locator.getPathVarMap();
    return new RequestMeta(referenceConfig, swaggerRestOperation, pathParams);
  }

  private CseClientHttpResponse invoke(Object[] args) {
    Invocation invocation =
        InvocationFactory.forConsumer(requestMeta.getReferenceConfig(),
            requestMeta.getOperationMeta(),
            args);
    invocation.getHandlerContext().put(RestConst.REST_CLIENT_REQUEST_PATH,
        this.uri.getRawPath() + "?" + this.uri.getRawQuery());

    if (context != null) {
      invocation.addContext(context);
    }

    invocation.getHandlerContext().put(RestConst.CONSUMER_HEADER, httpHeaders);
    Response response = doInvoke(invocation);

    if (response.isSuccessed()) {
      return new CseClientHttpResponse(response);
    }

    throw ExceptionFactory.convertConsumerException((Throwable) response.getResult());
  }

  protected Response doInvoke(Invocation invocation) {
    return InvokerUtils.innerSyncInvoke(invocation);
  }

  private Object[] collectArguments() {
    HttpServletRequest mockRequest = new CommonToHttpServletRequest(requestMeta.getPathParams(), queryParams,
        httpHeaders, requestBody, requestMeta.getSwaggerRestOperation().isFormData());
    return RestCodec.restToArgs(mockRequest, requestMeta.getSwaggerRestOperation());
  }
}
