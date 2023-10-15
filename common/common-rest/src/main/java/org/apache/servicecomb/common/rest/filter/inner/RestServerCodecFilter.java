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
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.filter.ProviderFilter;
import org.apache.servicecomb.foundation.common.utils.AsyncUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.foundation.vertx.stream.BufferOutputStream;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.vertx.core.MultiMap;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.core.HttpHeaders;

public class RestServerCodecFilter implements ProviderFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestServerCodecFilter.class);

  public static final String NAME = "rest-server-codec";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
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
    return nextNode.onFilter(invocation);
  }

  protected void decodeRequest(Invocation invocation) {
    HttpServletRequestEx requestEx = invocation.getRequestEx();

    OperationMeta operationMeta = invocation.getOperationMeta();
    RestOperationMeta restOperationMeta = operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
    Map<String, Object> swaggerArguments = RestCodec.restToArgs(requestEx, restOperationMeta);
    invocation.setSwaggerArguments(swaggerArguments);
  }

  protected CompletableFuture<Response> encodeResponse(Invocation invocation, Response response) {
    invocation.onEncodeResponseStart(response);

    HttpTransportContext transportContext = invocation.getTransportContext();

    // TODO: response support JsonView
    ProduceProcessor produceProcessor = ProduceProcessorManager.INSTANCE
        .createProduceProcessor(invocation.getOperationMeta(), response.getStatusCode(),
            invocation.getRequestEx().getHeader(HttpHeaders.ACCEPT), null);
    HttpServletResponseEx responseEx = transportContext.getResponseEx();
    boolean download = isDownloadFileResponseType(invocation, response);

    return encodeResponse(response, download, produceProcessor, responseEx);
  }

  public static CompletableFuture<Response> encodeResponse(Response response, boolean download,
      ProduceProcessor produceProcessor, HttpServletResponseEx responseEx) {
    responseEx.setStatus(response.getStatusCode());
    copyHeadersToHttpResponse(response.getHeaders(), responseEx);

    if (download) {
      return CompletableFuture.completedFuture(response);
    }

    responseEx.setContentType(produceProcessor.getName());
    try (BufferOutputStream output = new BufferOutputStream(Unpooled.compositeBuffer())) {
      produceProcessor.encodeResponse(output, response.getResult());

      responseEx.setBodyBuffer(output.getBuffer());

      return CompletableFuture.completedFuture(response);
    } catch (Throwable e) {
      LOGGER.error("internal service error must be fixed.", e);
      try (BufferOutputStream output = new BufferOutputStream(Unpooled.compositeBuffer())) {
        responseEx.setStatus(500);
        produceProcessor.encodeResponse(output, new CommonExceptionData("500", "Internal Server Error"));
        responseEx.setBodyBuffer(output.getBuffer());
        return CompletableFuture.completedFuture(response);
      } catch (Throwable e1) {
        // we have no idea how to do, no response given to client.
        return AsyncUtils.completeExceptionally(e);
      }
    }
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

  public static void copyHeadersToHttpResponse(MultiMap headers, HttpServletResponseEx responseEx) {
    if (headers == null) {
      return;
    }

    headers.remove(CONTENT_LENGTH);
    headers.remove(TRANSFER_ENCODING);
    for (Entry<String, String> entry : headers.entries()) {
      responseEx.addHeader(entry.getKey(), entry.getValue());
    }
  }
}
