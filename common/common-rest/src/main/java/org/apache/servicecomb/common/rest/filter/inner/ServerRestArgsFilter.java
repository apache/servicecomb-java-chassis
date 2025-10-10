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

package org.apache.servicecomb.common.rest.filter.inner;

import static org.apache.servicecomb.common.rest.filter.inner.RestServerCodecFilter.isDownloadFileResponseType;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.RestCodec;
import org.apache.servicecomb.common.rest.codec.produce.ProduceEventStreamProcessor;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessor;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.concurrency.SuppressedRunnableWrapper;
import org.apache.servicecomb.foundation.common.utils.PartUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.foundation.vertx.stream.BufferOutputStream;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

import io.vertx.core.buffer.Buffer;

public class ServerRestArgsFilter implements HttpServerFilter {
  private static final boolean enabled = DynamicPropertyFactory.getInstance().getBooleanProperty
      ("servicecomb.http.filter.server.serverRestArgs.enabled", true).get();

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerRestArgsFilter.class);

  @Override
  public int getOrder() {
    return -100;
  }

  @Override
  public boolean enabled() {
    return enabled;
  }

  @Override
  public Response afterReceiveRequest(Invocation invocation, HttpServletRequestEx requestEx) {
    OperationMeta operationMeta = invocation.getOperationMeta();
    RestOperationMeta restOperationMeta = operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
    Map<String, Object> swaggerArguments = RestCodec.restToArgs(requestEx, restOperationMeta);
    invocation.setSwaggerArguments(swaggerArguments);
    return null;
  }

  @Override
  public CompletableFuture<Void> beforeSendResponseAsync(Invocation invocation, HttpServletResponseEx responseEx) {
    Response response = (Response) responseEx.getAttribute(RestConst.INVOCATION_HANDLER_RESPONSE);
    ProduceProcessor produceProcessor =
        (ProduceProcessor) responseEx.getAttribute(RestConst.INVOCATION_HANDLER_PROCESSOR);
    boolean failed = response.getResult() instanceof InvocationException;
    if (!failed && isDownloadFileResponseType(invocation, response)) {
      return responseEx.sendPart(PartUtils.getSinglePart(null, response.getResult()));
    }

    if (isServerSendEvent(response)) {
      produceProcessor = new ProduceEventStreamProcessor();
      responseEx.setContentType(produceProcessor.getName() + "; charset=utf-8");
      return writeServerSendEvent(invocation, response, produceProcessor, responseEx);
    }

    responseEx.setContentType(produceProcessor.getName() + "; charset=utf-8");
    CompletableFuture<Void> future = new CompletableFuture<>();
    try (BufferOutputStream output = new BufferOutputStream(Buffer.buffer())) {
      if (failed) {
        produceProcessor.encodeResponse(output, ((InvocationException) response.getResult()).getErrorData());
      } else {
        produceProcessor.encodeResponse(output, response.getResult());
      }

      responseEx.setBodyBuffer(output.getBuffer());
      future.complete(null);
    } catch (Throwable e) {
      future.completeExceptionally(ExceptionFactory.convertProducerException(e));
    }
    return future;
  }

  public static boolean isServerSendEvent(Response response) {
    return response.getResult() instanceof Publisher<?>;
  }

  private static CompletableFuture<Void> writeServerSendEvent(Invocation invocation, Response response,
      ProduceProcessor produceProcessor, HttpServletResponseEx responseEx) {
    responseEx.setChunkedForEvent(true);
    CompletableFuture<Void> result = new CompletableFuture<>();
    Publisher<?> publisher = response.getResult();
    publisher.subscribe(new Subscriber<Object>() {
      Subscription subscription;

      @Override
      public void onSubscribe(Subscription s) {
        s.request(1);
        subscription = s;
      }

      @Override
      public void onNext(Object o) {
        writeResponse(responseEx, produceProcessor, o, response).thenApply(r -> {
            subscription.request(1);
            return r;
          })
          .exceptionally(e -> {
            new SuppressedRunnableWrapper(() -> subscription.cancel()).run();
            new SuppressedRunnableWrapper(() -> result.completeExceptionally(e)).run();
            return response;
          });
      }

      @Override
      public void onError(Throwable t) {
        result.completeExceptionally(t);
      }

      @Override
      public void onComplete() {
        result.complete(null);
      }
    });
    return result;
  }

  private static CompletableFuture<Response> writeResponse(
      HttpServletResponseEx responseEx, ProduceProcessor produceProcessor, Object data, Response response) {
    try (BufferOutputStream output = new BufferOutputStream(Buffer.buffer())) {
      produceProcessor.encodeResponse(output, data);
      return responseEx.sendBuffer(output.getBuffer())
          .thenApply(v -> {
            try {
              responseEx.flushBuffer();
              return response;
            } catch (IOException e) {
              LOGGER.warn("Failed to flush buffer for Server Send Events", e);
              throw new IllegalStateException("Failed to flush buffer for Server Send Events", e);
            }
          });
    } catch (Throwable e) {
      LOGGER.error("internal service error must be fixed.", e);
      return CompletableFuture.failedFuture(e);
    }
  }
}
