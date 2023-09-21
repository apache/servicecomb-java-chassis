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

package org.apache.servicecomb.common.rest.codec.param;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.servicecomb.codec.protobuf.utils.ScopedProtobufSchemaManager;
import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.RootDeserializer;
import org.apache.servicecomb.foundation.protobuf.RootSerializer;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyWrapper;
import org.apache.servicecomb.foundation.vertx.stream.BufferOutputStream;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

@SuppressWarnings("rawtypes")
public class BodyProcessorCreator implements ParamValueProcessorCreator<RequestBody> {
  private static final Logger LOGGER = LoggerFactory.getLogger(BodyProcessorCreator.class);

  public static final String REQUEST_BODY_NAME = "X_REQUEST";

  public static final String EXT_ID = "protobuf";

  public static final String PARAM_TYPE = "body";

  private static final JavaType OBJECT_TYPE = SimpleType.constructUnsafe(Object.class);

  // This configuration is used for temporary use only. Do not use it if you are sure how it works. And may be deleted in future.
  private static final boolean decodeAsObject = LegacyPropertyFactory
      .getBooleanProperty("servicecomb.rest.parameter.decodeAsObject", false);

  private static final Object LOCK = new Object();

  public static class BodyProcessor implements ParamValueProcessor {
    // Producer target type. For consumer, is null.
    protected JavaType targetType;

    protected Class<?> serialViewClass;

    protected boolean isRequired;

    protected OpenAPI openAPI;

    protected ScopedProtobufSchemaManager scopedProtobufSchemaManager;

    protected List<String> supportedContentTypes = new ArrayList<>();

    protected OperationMeta operationMeta;

    protected RequestBody requestBody;

    public BodyProcessor(OperationMeta operationMeta, JavaType targetType, RequestBody requestBody) {
      this.requestBody = requestBody;
      if (!StringUtils.isEmpty((String) this.requestBody.getExtensions()
          .get(SwaggerConst.EXT_JSON_VIEW))) {
        try {
          this.serialViewClass = Class.forName((String) this.requestBody.getExtensions()
              .get(SwaggerConst.EXT_JSON_VIEW));
        } catch (Throwable e) {
          //ignore
          LOGGER.warn("Failed to create body processor {}, annotation @JsonView may be invalid", serialViewClass, e);
        }
      }

      this.targetType = targetType;
      this.isRequired = this.requestBody.getRequired() != null && this.requestBody.getRequired();
      if (this.requestBody.getContent() != null) {
        supportedContentTypes.addAll(this.requestBody.getContent().keySet());
      }

      if (operationMeta != null) {
        this.operationMeta = operationMeta;
        this.openAPI = operationMeta.getSchemaMeta().getSwagger();
        if (supportedContentTypes.contains(SwaggerConst.PROTOBUF_TYPE)) {
          this.scopedProtobufSchemaManager = getOrCreateScopedProtobufSchemaManager(
              operationMeta.getMicroserviceMeta());
        }
      }
    }

    private ScopedProtobufSchemaManager getOrCreateScopedProtobufSchemaManager(MicroserviceMeta microserviceMeta) {
      ScopedProtobufSchemaManager scopedProtobufSchemaManager = microserviceMeta.getExtData(EXT_ID);
      if (scopedProtobufSchemaManager == null) {
        synchronized (LOCK) {
          scopedProtobufSchemaManager = microserviceMeta.getExtData(EXT_ID);
          if (scopedProtobufSchemaManager == null) {
            scopedProtobufSchemaManager = new ScopedProtobufSchemaManager();
            microserviceMeta.putExtData(EXT_ID, scopedProtobufSchemaManager);
          }
        }
      }
      return scopedProtobufSchemaManager;
    }

    @Override
    public Class<?> getSerialViewClass() {
      return serialViewClass;
    }

    @Override
    public Object getValue(HttpServletRequest request) throws Exception {
      Object result = getValueImpl(request);
      if (result == null && this.isRequired) {
        throw new InvocationException(Status.BAD_REQUEST, "Body parameter is required.");
      }
      return result;
    }

    private Object getValueImpl(HttpServletRequest request) throws IOException {
      Object body = request.getAttribute(RestConst.BODY_PARAMETER);
      if (body != null) {
        return convertValue(body, targetType);
      }

      // edge support convert from form-data or x-www-form-urlencoded to json automatically
      String contentType = validContentType(request.getContentType());
      if (contentType.equals(MediaType.MULTIPART_FORM_DATA)
          || contentType.equals(MediaType.APPLICATION_FORM_URLENCODED)) {
        return convertValue(request.getParameterMap(), targetType);
      }

      // for standard HttpServletRequest, getInputStream will never return null
      // but for mocked HttpServletRequest, maybe get a null
      //  like org.apache.servicecomb.provider.springmvc.reference.ClientToHttpServletRequest
      InputStream inputStream = request.getInputStream();
      if (inputStream == null) {
        return null;
      }

      if (!supportedContentTypes.contains(contentType)) {
        throw new IllegalArgumentException(String.format("operation %s not support content-type %s",
            operationMeta.getSchemaQualifiedName(), contentType));
      }

      if (SwaggerConst.PROTOBUF_TYPE.equals(contentType)) {
        ProtoMapper protoMapper = scopedProtobufSchemaManager
            .getOrCreateProtoMapper(openAPI, operationMeta.getSchemaId(),
                REQUEST_BODY_NAME,
                requestBody.getContent().get(SwaggerConst.PROTOBUF_TYPE).getSchema());
        RootDeserializer<PropertyWrapper<Object>> deserializer = protoMapper.getDeserializerSchemaManager()
            .createRootDeserializer(protoMapper.getProto().getMessage(REQUEST_BODY_NAME), targetType);
        PropertyWrapper<Object> result = deserializer.deserialize(inputStream.readAllBytes());
        return result.getValue();
      }

      // For application/json and text/plain
      try {
        if (MediaType.TEXT_PLAIN.equals(contentType) &&
            targetType != null && String.class.equals(targetType.getRawClass())) {
          return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
        ObjectReader reader = serialViewClass != null
            ? RestObjectMapperFactory.getRestObjectMapper().readerWithView(serialViewClass)
            : RestObjectMapperFactory.getRestObjectMapper().reader();
        if (decodeAsObject) {
          return reader.forType(OBJECT_TYPE).readValue(inputStream);
        }
        return reader.forType(targetType == null ? OBJECT_TYPE : targetType)
            .readValue(inputStream);
      } catch (MismatchedInputException e) {
        // there is no way to detect InputStream is empty, so have to catch the exception
        if (!isRequired && e.getMessage().contains("No content to map due to end-of-input")) {
          LOGGER.info("Empty content and required is false, taken as null");
          return null;
        }
        throw e;
      }
    }

    private String validContentType(String type) {
      if (StringUtils.isEmpty(type)) {
        if (supportedContentTypes.size() == 0) {
          throw new IllegalArgumentException("operation do not have any content type support.");
        }
        if (supportedContentTypes.contains(MediaType.APPLICATION_JSON)) {
          return MediaType.APPLICATION_JSON;
        }
        return supportedContentTypes.get(0);
      }
      ContentType contentType = ContentType.parse(type);
      return contentType.getMimeType();
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
      String userContentType = clientRequest.getHeaders().get(HttpHeaders.CONTENT_TYPE);
      String contentType = validContentType(userContentType);
      if (StringUtils.isEmpty(userContentType)) {
        clientRequest.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
      }
      if (arg != null) {
        Buffer buffer = createBodyBuffer(contentType, arg);
        clientRequest.write(buffer);
      }
    }

    /**
     * Deserialize body object into body buffer, according to the Content-Type.
     */
    private Buffer createBodyBuffer(String contentType, Object arg) throws IOException {
      if (SwaggerConst.PROTOBUF_TYPE.equals(contentType)) {
        ProtoMapper protoMapper = scopedProtobufSchemaManager
            .getOrCreateProtoMapper(openAPI, operationMeta.getSchemaId(),
                REQUEST_BODY_NAME,
                requestBody.getContent().get(SwaggerConst.PROTOBUF_TYPE).getSchema());
        RootSerializer serializer = protoMapper.getSerializerSchemaManager()
            .createRootSerializer(protoMapper.getProto().getMessage(REQUEST_BODY_NAME),
                Object.class);
        Map<String, Object> bodyArg = new HashMap<>(1);
        bodyArg.put("value", arg);
        return new BufferImpl().appendBytes(serializer.serialize(bodyArg));
      }

      // For application/json and text/plain
      try (BufferOutputStream output = new BufferOutputStream()) {
        if (MediaType.TEXT_PLAIN.equals(contentType) && (arg instanceof String)) {
          output.write(((String) arg).getBytes(StandardCharsets.UTF_8));
        } else {
          RestObjectMapperFactory.getConsumerWriterMapper().writeValue(output, arg);
        }
        return output.getBuffer();
      }
    }

    @Override
    public String getParameterPath() {
      return "";
    }

    @Override
    public String getProcessorType() {
      return PARAM_TYPE;
    }
  }

  public static class RawJsonBodyProcessor implements ParamValueProcessor {
    protected JavaType targetType;

    protected Class<?> serialViewClass;

    protected boolean isRequired;

    public RawJsonBodyProcessor(JavaType targetType, boolean isRequired) {
      this(targetType, null, isRequired);
    }

    public RawJsonBodyProcessor(JavaType targetType, String serialViewClass, boolean isRequired) {
      if (!StringUtils.isEmpty(serialViewClass)) {
        try {
          this.serialViewClass = Class.forName(serialViewClass);
        } catch (Throwable e) {
          //ignore
          LOGGER.warn("Failed to create body processor {}, annotation @JsonView may be invalid", serialViewClass, e);
        }
      }
      this.targetType = targetType;
      this.isRequired = isRequired;
    }

    @Override
    public Object getValue(HttpServletRequest request) throws Exception {
      Object body = request.getAttribute(RestConst.BODY_PARAMETER);
      if (body != null) {
        return convertValue(body, targetType);
      }

      InputStream inputStream = request.getInputStream();
      if (inputStream == null) {
        return null;
      }

      return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
      if (arg instanceof String) {
        clientRequest.putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        clientRequest.write(Buffer.buffer((String) arg));
        return;
      }

      throw new IllegalArgumentException("@RawJsonRequestBody only supports string type.");
    }

    @Override
    public String getParameterPath() {
      return "";
    }

    @Override
    public String getProcessorType() {
      return PARAM_TYPE;
    }
  }

  public BodyProcessorCreator() {
    ParamValueProcessorCreatorManager.INSTANCE.register(PARAM_TYPE, this);
  }

  @Override
  public ParamValueProcessor create(OperationMeta operationMeta, String parameterName,
      RequestBody parameter, Type genericParamType) {
    JavaType targetType =
        genericParamType == null ? null : TypeFactory.defaultInstance().constructType(genericParamType);
    boolean rawJson = SwaggerUtils.isRawJsonType(parameter);
    if (rawJson) {
      return new RawJsonBodyProcessor(targetType, (String) parameter.getExtensions()
          .get(SwaggerConst.EXT_JSON_VIEW),
          parameter.getRequired() != null && parameter.getRequired());
    }

    return new BodyProcessor(operationMeta, targetType, parameter);
  }
}
