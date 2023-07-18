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

package org.apache.servicecomb.swagger.generator.springmvc.processor.annotation;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JavaType;
import com.google.inject.util.Types;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RequestPartAnnotationProcessor extends
    AbstractSpringmvcParameterProcessor<RequestPart> {
  @Override
  public Type getProcessType() {
    return RequestPart.class;
  }

  @Override
  public String getParameterName(RequestPart annotation) {
    String value = annotation.value();
    if (value.isEmpty()) {
      value = annotation.name();
    }
    return value;
  }

  @Override
  public HttpParameterType getHttpParameterType(RequestPart parameterAnnotation) {
    return HttpParameterType.FORM;
  }

  @Override
  public void fillParameter(OpenAPI swagger, Operation operation, Parameter parameter, JavaType type,
      RequestPart requestPart) {

  }

  @Override
  public void fillRequestBody(OpenAPI swagger, Operation operation, RequestBody requestBody, String parameterName,
      JavaType type, RequestPart requestPart) {
    if (requestBody.getContent() == null) {
      requestBody.setContent(new Content());
    }
    if (requestBody.getContent().get(SwaggerConst.FILE_MEDIA_TYPE) == null) {
      requestBody.getContent().addMediaType(SwaggerConst.FILE_MEDIA_TYPE,
          new io.swagger.v3.oas.models.media.MediaType());
    }
    if (requestBody.getContent().get(SwaggerConst.FILE_MEDIA_TYPE).getSchema() == null) {
      Schema<?> schema = new Schema<>();
      schema.setProperties(new HashMap<>());
      requestBody.getContent().get(SwaggerConst.FILE_MEDIA_TYPE)
          .setSchema(schema);
    }
    // RequestPart used with MultipartFile and simple types.
    // MultipartFile is processed by type processor.
    if (!MultipartFile.class.equals(type.getRawClass()) &&
        !Types.newParameterizedType(List.class, MultipartFile.class).equals(type.getRawClass()) &&
        !MultipartFile[].class.equals(type.getRawClass())) {
      Schema schema = SwaggerUtils.resolveTypeSchemas(swagger, type);
      requestBody.getContent().get(SwaggerConst.FILE_MEDIA_TYPE).getSchema().getProperties().put(parameterName, schema);
    }
  }
}
