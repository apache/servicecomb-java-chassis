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

import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.apache.servicecomb.transport.rest.client.RestClientExceptionCodes.FAILED_TO_ENCODE_REST_CLIENT_REQUEST;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.common.rest.codec.RestCodec;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.common.rest.codec.query.QueryCodec;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationConfig;
import org.apache.servicecomb.foundation.common.utils.StringBuilderUtils;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

/**
 * encode all send data except upload
 */
public class RestClientEncoder {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestClientEncoder.class);

  public static final int FORM_BUFFER_SIZE = 1024;

  public void encode(Invocation invocation) {
    try {
      invocation.getInvocationStageTrace().startConsumerEncodeRequest();
      EncoderSession encoderSession = new EncoderSession(invocation);
      encoderSession.doEncode();
      invocation.getInvocationStageTrace().finishConsumerEncodeRequest();
    } catch (Exception e) {
      throw new InvocationException(BAD_REQUEST, FAILED_TO_ENCODE_REST_CLIENT_REQUEST, e.getMessage(), e);
    }
  }

  public static class EncoderSession {
    protected final Invocation invocation;

    protected final RestClientTransportContext transportContext;

    protected final RestClientRequestParameters requestParameters;

    protected final HttpClientRequest httpClientRequest;

    public EncoderSession(Invocation invocation) {
      this.invocation = invocation;
      this.transportContext = invocation.getTransportContext();
      this.requestParameters = this.transportContext.getRequestParameters();
      this.httpClientRequest = this.transportContext.getHttpClientRequest();
    }

    protected void doEncode() throws Exception {
      RestClientEncoder.LOGGER.debug("encode rest client request, operation={}, method={}, endpoint={}, uri={}.",
          invocation.getMicroserviceQualifiedName(),
          httpClientRequest.getMethod(),
          invocation.getEndpoint().getEndpoint(),
          httpClientRequest.getURI());

      swaggerArgumentsToRequest();

      writeCookies(requestParameters.getCookieMap());
      writeScbHeaders();
      writeForm(requestParameters.getFormMap());
    }

    protected void swaggerArgumentsToRequest() throws Exception {
      RestCodec.argsToRest(invocation.getSwaggerArguments(),
          transportContext.getRestOperationMeta(), requestParameters);
    }

    protected void writeCookies(Map<String, String> cookieMap) {
      if (CollectionUtils.isEmpty(cookieMap)) {
        return;
      }

      StringBuilder builder = new StringBuilder();
      for (Entry<String, String> entry : cookieMap.entrySet()) {
        builder.append(entry.getKey())
            .append('=')
            .append(entry.getValue())
            .append("; ");
      }
      StringBuilderUtils.deleteLast(builder, 2);
      httpClientRequest.putHeader(HttpHeaders.COOKIE, builder.toString());
    }

    protected void writeScbHeaders() throws JsonProcessingException {
      OperationConfig operationConfig = invocation.getOperationMeta().getConfig();
      if (operationConfig.isClientRequestHeaderFilterEnabled()) {
        return;
      }

      httpClientRequest.putHeader(CoreConst.TARGET_MICROSERVICE, invocation.getMicroserviceName());
      httpClientRequest.putHeader(CoreConst.CSE_CONTEXT,
          RestObjectMapperFactory.getRestObjectMapper().writeValueAsString(invocation.getContext()));
    }

    protected void writeForm(Map<String, Object> formMap) throws Exception {
      if (requestParameters.getUploads() == null) {
        writeUrlEncodedForm(formMap);
        return;
      }

      writeChunkedForm(formMap);
    }

    protected void writeUrlEncodedForm(Map<String, Object> formMap) throws Exception {
      if (formMap == null) {
        return;
      }

      httpClientRequest.putHeader(CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);

      Buffer bodyBuffer = genUrlEncodedFormBuffer(formMap);
      requestParameters.setBodyBuffer(bodyBuffer);
    }

    protected Buffer genUrlEncodedFormBuffer(Map<String, Object> formMap) throws Exception {
      Buffer buffer = Buffer.buffer(RestClientEncoder.FORM_BUFFER_SIZE);
      for (Entry<String, Object> entry : formMap.entrySet()) {
        writeCharSequence(buffer, entry.getKey());
        buffer.appendByte(((byte) '='));

        String value = QueryCodec.convertToString(entry.getValue());
        String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
        writeCharSequence(buffer, encodedValue);

        buffer.appendByte(((byte) '&'));
      }

      return buffer;
    }

    protected void writeChunkedForm(Map<String, Object> formMap) throws Exception {
      String boundary = transportContext.getOrCreateBoundary();

      httpClientRequest.setChunked(true);
      httpClientRequest.putHeader(CONTENT_TYPE, MULTIPART_FORM_DATA + "; charset=UTF-8; boundary=" + boundary);

      if (formMap == null) {
        return;
      }

      Buffer bodyBuffer = genChunkedFormBuffer(formMap, boundary);
      requestParameters.setBodyBuffer(bodyBuffer);
    }

    protected Buffer genChunkedFormBuffer(Map<String, Object> formMap, String boundary) throws Exception {
      Buffer buffer = Buffer.buffer(RestClientEncoder.FORM_BUFFER_SIZE);
      for (Entry<String, Object> entry : formMap.entrySet()) {
        Object content = entry.getValue();
        if (content instanceof List<?>) {
          for (Object item : ((List<?>) content)) {
            writeFormData(buffer, boundary, entry.getKey(), item);
          }
        } else {
          writeFormData(buffer, boundary, entry.getKey(), entry.getValue());
        }
      }
      return buffer;
    }

    private void writeFormData(Buffer buffer, String boundary, String key, Object data) throws Exception {
      writeCharSequence(buffer, "\r\n--");
      writeCharSequence(buffer, boundary);
      writeCharSequence(buffer, "\r\nContent-Disposition: form-data; name=\"");
      writeCharSequence(buffer, key);
      writeCharSequence(buffer, "\"\r\n\r\n");

      String value = QueryCodec.convertToString(data);
      writeCharSequence(buffer, value);
    }
  }

  protected static void writeCharSequence(Buffer buffer, String value) {
    buffer.appendString(value, "UTF-8");
  }

  public static Buffer genFileBoundaryBuffer(Part part, String name, String boundary) {
    Buffer buffer = Buffer.buffer(RestClientEncoder.FORM_BUFFER_SIZE);

    writeCharSequence(buffer, "\r\n--");
    writeCharSequence(buffer, boundary);
    writeCharSequence(buffer, "\r\nContent-Disposition: form-data; name=\"");
    writeCharSequence(buffer, name);
    writeCharSequence(buffer, "\"; filename=\"");
    writeCharSequence(buffer, String.valueOf(part.getSubmittedFileName()));
    writeCharSequence(buffer, "\"\r\n");

    writeCharSequence(buffer, "Content-Type: ");
    writeCharSequence(buffer, part.getContentType());
    writeCharSequence(buffer, "\r\n");

    writeCharSequence(buffer, "Content-Transfer-Encoding: binary\r\n");

    writeCharSequence(buffer, "\r\n");

    return buffer;
  }

  public static Buffer genBoundaryEndBuffer(String boundary) {
    Buffer buffer = Buffer.buffer(RestClientEncoder.FORM_BUFFER_SIZE);

    writeCharSequence(buffer, "\r\n--");
    writeCharSequence(buffer, boundary);
    writeCharSequence(buffer, "--\r\n");

    return buffer;
  }
}
