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

package org.apache.servicecomb.provider.springmvc.reference.async;

import java.io.OutputStream;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.provider.springmvc.reference.CseClientHttpRequest;
import org.apache.servicecomb.provider.springmvc.reference.CseClientHttpResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AsyncClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.concurrent.CompletableToListenableFutureAdapter;
import org.springframework.util.concurrent.ListenableFuture;

import io.netty.handler.codec.http.QueryStringDecoder;

public class CseAsyncClientHttpRequest extends CseClientHttpRequest implements AsyncClientHttpRequest {

  CseAsyncClientHttpRequest() {
  }

  CseAsyncClientHttpRequest(URI uri, HttpMethod method) {
    this.setUri(uri);
    this.setMethod(method);
  }

  @Override
  public OutputStream getBody() {
    return null;
  }

  private ListenableFuture<ClientHttpResponse> invoke(Object[] args) {
    Invocation invocation = prepareInvocation(args);
    invocation.getHandlerContext().put(RestConst.CONSUMER_HEADER, this.getHeaders());
    CompletableFuture<ClientHttpResponse> clientHttpResponseCompletableFuture = doAsyncInvoke(invocation);
    return new CompletableToListenableFutureAdapter<ClientHttpResponse>(clientHttpResponseCompletableFuture);
  }

  protected CompletableFuture<ClientHttpResponse> doAsyncInvoke(Invocation invocation) {
    CompletableFuture<ClientHttpResponse> completableFuture = new CompletableFuture<>();
    InvokerUtils.reactiveInvoke(invocation, (Response response) -> {
      if (response.isSuccessed()) {
        completableFuture.complete(new CseClientHttpResponse(response));
      } else {
        completableFuture.completeExceptionally(response.getResult());
      }
    });
    return completableFuture;
  }


  @Override
  public ListenableFuture<ClientHttpResponse> executeAsync() {
    this.setPath(findUriPath(this.getURI()));
    this.setRequestMeta(createRequestMeta(this.getMethod().name(), this.getURI()));
    QueryStringDecoder queryStringDecoder = new QueryStringDecoder(this.getURI().getRawSchemeSpecificPart());
    this.setQueryParams(queryStringDecoder.parameters());
    Object[] args = this.collectArguments();
    return this.invoke(args);
  }
}
