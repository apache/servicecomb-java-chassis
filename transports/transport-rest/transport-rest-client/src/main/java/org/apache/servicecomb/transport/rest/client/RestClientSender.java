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
package org.apache.servicecomb.transport.rest.client;

import static org.apache.servicecomb.transport.rest.client.RestClientEncoder.genBoundaryEndBuffer;
import static org.apache.servicecomb.transport.rest.client.RestClientEncoder.genFileBoundaryBuffer;

import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.http.HttpStatus;
import org.apache.servicecomb.foundation.vertx.executor.VertxContextExecutor;
import org.apache.servicecomb.foundation.vertx.http.ReadStreamPart;
import org.apache.servicecomb.foundation.vertx.stream.PumpFromPart;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.rxjava3.FlowableHelper;
import jakarta.servlet.http.Part;

public class RestClientSender {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestClientSender.class);

  protected final Invocation invocation;

  protected final RestClientTransportContext transportContext;

  protected final RestClientRequestParameters requestParameters;

  protected final HttpClientRequest httpClientRequest;

  protected final CompletableFuture<Response> future = new CompletableFuture<>();

  public RestClientSender(Invocation invocation) {
    this.invocation = invocation;
    this.transportContext = invocation.getTransportContext();
    this.requestParameters = transportContext.getRequestParameters();
    this.httpClientRequest = transportContext.getHttpClientRequest();
  }

  public CompletableFuture<Response> send() {
    invocation.getInvocationStageTrace().startConsumerSendRequest();
    httpClientRequest.response().compose(this::processResponse).onFailure(future::completeExceptionally);

    CompletableFuture<Response> actualFuture = future.whenComplete(this::afterSend);
    VertxContextExecutor.create(transportContext.getVertxContext()).execute(this::runInVertxContext);
    return actualFuture;
  }

  protected void runInVertxContext() {
    sendInVertxContext()
        .whenComplete((v, e) -> {
          if (e != null) {
            future.completeExceptionally(e);
          }
          invocation.getInvocationStageTrace().finishConsumerSendRequest();
          invocation.getInvocationStageTrace().startWaitResponse();
        });
  }

  protected CompletableFuture<Void> sendInVertxContext() {
    httpClientRequest.setTimeout(invocation.getOperationMeta().getConfig().getMsRequestTimeout());

    Multimap<String, Part> uploads = requestParameters.getUploads();
    if (uploads == null) {
      if (requestParameters.getBodyBuffer() != null) {
        return CompletableFuture.completedFuture(null).thenCompose(
            v -> httpClientRequest.end(requestParameters.getBodyBuffer()).toCompletionStage());
      } else {
        return CompletableFuture.completedFuture(null).thenCompose(
            v -> httpClientRequest.end().toCompletionStage());
      }
    }

    if (requestParameters.getBodyBuffer() != null) {
      httpClientRequest.write(requestParameters.getBodyBuffer());
    }
    return sendFiles();
  }

  protected CompletableFuture<Void> sendFiles() {
    CompletableFuture<Void> sendFileFuture = CompletableFuture.completedFuture(null);

    String boundary = transportContext.getOrCreateBoundary();
    for (Entry<String, Part> entry : requestParameters.getUploads().entries()) {
      // do not use part.getName() to get parameter name
      // because pojo consumer not easy to set name to part
      String name = entry.getKey();
      sendFileFuture = sendFileFuture.thenCompose(v -> sendFile(entry.getValue(), name, boundary));
    }

    return sendFileFuture.thenCompose(v -> httpClientRequest.end(genBoundaryEndBuffer(boundary)).toCompletionStage());
  }

  private CompletableFuture<Void> sendFile(Part part, String name, String boundary) {
    Buffer fileHeader = genFileBoundaryBuffer(part, name, boundary);
    httpClientRequest.write(fileHeader);

    return new PumpFromPart(transportContext.getVertxContext(), part)
        .toWriteStream(httpClientRequest, future::completeExceptionally)
        .whenComplete((v, e) -> {
          if (e != null) {
            LOGGER.debug("Failed to send file [{}:{}].", name, part.getSubmittedFileName(), e);
            return;
          }

          LOGGER.debug("finish send file [{}:{}].", name, part.getSubmittedFileName());
        });
  }

  protected Future<Object> processResponse(HttpClientResponse httpClientResponse) {
    Promise<Object> result = Promise.promise();
    transportContext.setHttpClientResponse(httpClientResponse);

    if (!HttpStatus.isSuccess(httpClientResponse.statusCode())) {
      httpClientResponse.body().compose(buffer -> {
        future.complete(createResponse(httpClientResponse, buffer));
        result.complete();
        return Future.succeededFuture();
      });
      return result.future();
    }

    if (transportContext.isDownloadFile()) {
      ReadStreamPart streamPart = new ReadStreamPart(transportContext.getVertxContext(), httpClientResponse);
      future.complete(createResponse(httpClientResponse, streamPart));
      result.complete();
      return result.future();
    }

    if (transportContext.isServerSendEvents()) {
      future.complete(createResponse(httpClientResponse, FlowableHelper.toFlowable(httpClientResponse)));
      result.complete();
      return result.future();
    }

    httpClientResponse.body().compose(buffer -> {
      future.complete(createResponse(httpClientResponse, buffer));
      result.complete();
      return Future.succeededFuture();
    });
    return result.future();
  }

  protected Response createResponse(HttpClientResponse httpClientResponse, Object result) {
    // http2's :status header will cause edge forward failed
    MultiMap headers = httpClientResponse.headers();
    headers.remove(":status");

    HttpStatus httpStatus = new HttpStatus(httpClientResponse.statusCode(), httpClientResponse.statusMessage());
    return Response
        .status(httpStatus)
        .setHeaders(headers)
        .entity(result);
  }

  protected void afterSend(Response response, Throwable throwable) {
    invocation.getInvocationStageTrace().finishWaitResponse();

    if (throwable != null) {
      LOGGER.error("rest client send or receive failed, operation={}, method={}, endpoint={}, uri={}.",
          invocation.getMicroserviceQualifiedName(),
          httpClientRequest.getMethod(),
          invocation.getEndpoint().getEndpoint(),
          httpClientRequest.getURI());
    }
  }
}
