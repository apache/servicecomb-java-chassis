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

package org.apache.servicecomb.swagger.generator.jaxrs.processor.annotation;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;

import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.FormParam;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FormParamAnnotationProcessor extends JaxrsParameterProcessor<FormParam> {
  @Override
  public Type getProcessType() {
    return FormParam.class;
  }

  @Override
  public String getParameterName(FormParam parameterAnnotation) {
    return parameterAnnotation.value();
  }

  @Override
  public HttpParameterType getHttpParameterType(FormParam parameterAnnotation) {
    return HttpParameterType.FORM;
  }

  @Override
  public void fillParameter(OpenAPI swagger, Operation operation, Parameter parameter, JavaType type,
      FormParam formParam) {

  }

  @Override
  public void fillRequestBody(OpenAPI swagger, Operation operation, RequestBody requestBody, String parameterName,
      JavaType type, FormParam formParam) {
    if (requestBody.getContent() == null) {
      requestBody.setContent(new Content());
    }

    String mediaType = SwaggerConst.FORM_MEDIA_TYPE;
    if (requestBody.getContent().get(SwaggerConst.FILE_MEDIA_TYPE) != null || isPart(type)) {
      mediaType = SwaggerConst.FILE_MEDIA_TYPE;
    }

    if (requestBody.getContent().get(mediaType) == null) {
      requestBody.getContent().addMediaType(mediaType,
          new io.swagger.v3.oas.models.media.MediaType());
    }
    if (requestBody.getContent().get(mediaType).getSchema() == null) {
      requestBody.getContent().get(mediaType)
          .setSchema(new MapSchema());
    }
    if (requestBody.getContent().get(mediaType).getSchema().getProperties() == null) {
      requestBody.getContent().get(mediaType)
          .getSchema().setProperties(new LinkedHashMap<>());
    }

    // FormParam used with Part and simple types.
    // Part is processed by type processor.
    if (!isPart(type)) {
      Schema schema = SwaggerUtils.resolveTypeSchemas(swagger, type);
      requestBody.getContent().get(mediaType).getSchema().getProperties().put(parameterName, schema);
    }
  }

  private boolean isPart(JavaType type) {
    return Part.class.equals(type.getRawClass());
  }
}
