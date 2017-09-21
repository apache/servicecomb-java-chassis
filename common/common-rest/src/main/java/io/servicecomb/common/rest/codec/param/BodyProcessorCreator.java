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

package io.servicecomb.common.rest.codec.param;

import java.io.InputStream;
import java.lang.reflect.Type;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.codec.RestClientRequest;
import io.servicecomb.common.rest.codec.RestObjectMapper;
import io.servicecomb.foundation.vertx.stream.BufferOutputStream;
import io.servicecomb.swagger.generator.core.utils.ClassUtils;
import io.swagger.models.parameters.Parameter;
import io.vertx.core.buffer.Buffer;

public class BodyProcessorCreator implements ParamValueProcessorCreator {

  public static final String PARAMTYPE = "body";

  public static class BodyProcessor implements ParamValueProcessor {
    protected JavaType targetType;

    public BodyProcessor(JavaType targetType) {
      this.targetType = targetType;
    }

    @Override
    public Object getValue(HttpServletRequest request) throws Exception {
      Object body = request.getAttribute(RestConst.BODY_PARAMETER);
      if (body != null) {
        return convertValue(body, targetType);
      }

      // for standard HttpServletRequest, getInputStream will never return null
      // but for mocked HttpServletRequest, maybe get a null
      //  like io.servicecomb.provider.springmvc.reference.ClientToHttpServletRequest
      InputStream inputStream = request.getInputStream();
      if (inputStream == null) {
        return null;
      }

      String contentType = request.getContentType();
      if (contentType != null && !contentType.startsWith(MediaType.APPLICATION_JSON)) {
        // TODO: we should consider body encoding
        return IOUtils.toString(inputStream, "UTF-8");
      }
      return RestObjectMapper.INSTANCE.readValue(inputStream, targetType);
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
      try (BufferOutputStream output = new BufferOutputStream()) {
        clientRequest.putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        RestObjectMapper.INSTANCE.writeValue(output, arg);
        clientRequest.write(output.getBuffer());
      }
    }

    @Override
    public String getParameterPath() {
      return "";
    }

    @Override
    public String getProcessorType() {
      return PARAMTYPE;
    }
  }

  public static class RawJsonBodyProcessor extends BodyProcessor {

    public RawJsonBodyProcessor(JavaType targetType) {
      super(targetType);
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

      // TODO: we should consider body encoding
      return IOUtils.toString(inputStream, "UTF-8");
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
      clientRequest.putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
      clientRequest.write(Buffer.buffer((String) arg));
    }
  }

  public BodyProcessorCreator() {
    ParamValueProcessorCreatorManager.INSTANCE.register(PARAMTYPE, this);
  }

  @Override
  public ParamValueProcessor create(Parameter parameter, Type genericParamType) {
    JavaType targetType = TypeFactory.defaultInstance().constructType(genericParamType);
    boolean rawJson = ClassUtils.isRawJsonType(parameter);
    if (genericParamType.getTypeName().equals(String.class.getTypeName()) && rawJson) {
      return new RawJsonBodyProcessor(targetType);
    }

    return new BodyProcessor(targetType);
  }
}
