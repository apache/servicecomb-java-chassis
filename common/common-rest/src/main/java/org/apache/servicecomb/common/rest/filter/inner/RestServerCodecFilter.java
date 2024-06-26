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

import static com.google.common.net.HttpHeaders.CONTENT_LENGTH;
import static com.google.common.net.HttpHeaders.TRANSFER_ENCODING;
import static org.apache.servicecomb.core.exception.Exceptions.toProducerResponse;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.common.rest.HttpTransportContext;
import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.RestCodec;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessor;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessorManager;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.filter.AbstractFilter;
import org.apache.servicecomb.core.filter.EdgeFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.filter.ProviderFilter;
import org.apache.servicecomb.foundation.common.utils.PartUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.foundation.vertx.stream.BufferOutputStream;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.context.TransportContext;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.core.HttpHeaders;

public class RestServerCodecFilter extends AbstractFilter implements ProviderFilter, EdgeFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestServerCodecFilter.class);

  public static final String NAME = "rest-server-codec";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public int getOrder() {
    // almost time, should be the first filter.
    return Filter.PROVIDER_SCHEDULE_FILTER_ORDER - 2000;
  }

  @Override
  public boolean enabledForTransport(String transport) {
    return CoreConst.RESTFUL.equals(transport);
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    return CompletableFuture.completedFuture(invocation)
        .thenAccept(this::decodeRequest)
        .thenCompose(v -> invokeNext(invocation, nextNode))
        .exceptionally(exception -> toProducerResponse(invocation, exception))
        .thenCompose(response -> encodeResponse(invocation, response));
  }

  protected CompletableFuture<Response> invokeNext(Invocation invocation, FilterNode nextNode) {
    if (invocation.isEdge()) {
      TransportContext transportContext = invocation.getTransportContext();
      return nextNode.onFilter(invocation).whenComplete((r, e) -> invocation.setTransportContext(transportContext));
    }
    return nextNode.onFilter(invocation);
  }

  protected void decodeRequest(Invocation invocation) {
    invocation.getInvocationStageTrace().startProviderDecodeRequest();
    HttpServletRequestEx requestEx = invocation.getRequestEx();

    OperationMeta operationMeta = invocation.getOperationMeta();
    RestOperationMeta restOperationMeta = operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
    Map<String, Object> swaggerArguments = RestCodec.restToArgs(requestEx, restOperationMeta);
    invocation.setSwaggerArguments(swaggerArguments);
    invocation.getInvocationStageTrace().finishProviderDecodeRequest();
  }

  protected CompletableFuture<Response> encodeResponse(Invocation invocation, Response response) {
    invocation.onEncodeResponseStart(response);
    HttpTransportContext transportContext = invocation.getTransportContext();
    HttpServletResponseEx responseEx = transportContext.getResponseEx();
    // TODO: response support JsonView
    ProduceProcessor produceProcessor = ProduceProcessorManager.INSTANCE
        .createProduceProcessor(invocation.getOperationMeta(), response.getStatusCode(),
            invocation.getRequestEx().getHeader(HttpHeaders.ACCEPT), null);

    return encodeResponse(invocation, response, produceProcessor, responseEx)
        .whenComplete((r, e) -> invocation.onEncodeResponseFinish());
  }

  private static boolean isFailedResponse(Response response) {
    return response.getResult() instanceof InvocationException;
  }

  private static CompletableFuture<Response> writePart(
      HttpServletResponseEx responseEx, Object data, Response response) {
    CompletableFuture<Response> result = new CompletableFuture<>();
    responseEx.sendPart(PartUtils.getSinglePart(null, data))
        .whenComplete((r, e) -> {
          if (e != null) {
            result.completeExceptionally(e);
            return;
          }
          result.complete(response);
        });
    return result;
  }

  private static CompletableFuture<Response> writeResponse(
      HttpServletResponseEx responseEx, ProduceProcessor produceProcessor, Object data, Response response,
      boolean commit) {
    try (BufferOutputStream output = new BufferOutputStream(Buffer.buffer())) {
      produceProcessor.encodeResponse(output, data);

      CompletableFuture<Response> result = new CompletableFuture<>();
      responseEx.setBodyBuffer(output.getBuffer()); // For extensions usage
      if (commit) {
        responseEx.setContentLength(output.getBuffer().length());
      }
      responseEx.sendBuffer(output.getBuffer()).whenComplete((v, e) -> {
        if (e != null) {
          result.completeExceptionally(e);
          return;
        }
        result.complete(response);
      });
      return result;
    } catch (Throwable e) {
      LOGGER.error("internal service error must be fixed.", e);
      responseEx.setStatus(500);
      return CompletableFuture.failedFuture(e);
    }
  }

  public static CompletableFuture<Response> encodeResponse(Invocation invocation, Response response,
      ProduceProcessor produceProcessor, HttpServletResponseEx responseEx) {
    responseEx.setStatus(response.getStatusCode());
    copyHeadersToHttpResponse(invocation, response.getHeaders(), responseEx);

    if (isFailedResponse(response)) {
      responseEx.setContentType(produceProcessor.getName());
      return writeResponse(responseEx, produceProcessor, ((InvocationException) response.getResult()).getErrorData(),
          response, true);
    }

    if (isDownloadFileResponseType(invocation, response)) {
      return writePart(responseEx, response.getResult(), response);
    }

    if (isServerSendEvent(response)) {
      responseEx.setContentType(produceProcessor.getName());
      return writeServerSendEvent(response, produceProcessor, responseEx);
    }

    responseEx.setContentType(produceProcessor.getName());
    return writeResponse(responseEx, produceProcessor, response.getResult(), response, true);
  }

  private static CompletableFuture<Response> writeServerSendEvent(Response response, ProduceProcessor produceProcessor,
      HttpServletResponseEx responseEx) {
    responseEx.setChunked(true);
    CompletableFuture<Response> result = new CompletableFuture<>();
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
        writeResponse(responseEx, produceProcessor, o, response, false).whenComplete((r, e) -> {
          if (e != null) {
            subscription.cancel();
            result.completeExceptionally(e);
            return;
          }
          subscription.request(1);
        });
      }

      @Override
      public void onError(Throwable t) {
        result.completeExceptionally(t);
      }

      @Override
      public void onComplete() {
        result.complete(response);
      }
    });
    return result;
  }

  /**
   * Check whether this response is a downloaded file response,
   * according to the schema recorded in {@link org.apache.servicecomb.swagger.invocation.response.ResponsesMeta}
   * and response status code.
   * @return true if this response is a downloaded file, otherwise false.
   */
  public static boolean isDownloadFileResponseType(Invocation invocation, Response response) {
    return Part.class.isAssignableFrom(
        invocation.findResponseType(response.getStatusCode()).getRawClass());
  }

  public static boolean isServerSendEvent(Response response) {
    return response.getResult() instanceof Publisher<?>;
  }

  public static void copyHeadersToHttpResponse(Invocation invocation, MultiMap headers,
      HttpServletResponseEx responseEx) {
    if (headers != null) {
      headers.remove(CONTENT_LENGTH);
      headers.remove(TRANSFER_ENCODING);
      for (Entry<String, String> entry : headers.entries()) {
        responseEx.addHeader(entry.getKey(), entry.getValue());
      }
    }

    if (invocation != null && responseEx.getHeader(CoreConst.TRACE_ID_NAME) == null) {
      responseEx.addHeader(CoreConst.TRACE_ID_NAME, invocation.getTraceId());
    }
  }
}
