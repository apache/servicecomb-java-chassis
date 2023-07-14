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

import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;
import org.apache.servicecomb.swagger.generator.core.processor.parameter.AbstractSerializableParameterProcessor;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import jakarta.ws.rs.FormParam;

@SuppressWarnings("rawtypes")
public class FormParamAnnotationProcessor extends AbstractSerializableParameterProcessor<FormParam> {
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
    Schema schema = SwaggerUtils.resolveTypeSchemas(swagger, type);
    if (requestBody.getContent() == null) {
      requestBody.setContent(new Content());
    }
    if (requestBody.getContent().get(SwaggerConst.FORM_MEDIA_TYPE) == null) {
      requestBody.getContent().addMediaType(SwaggerConst.FORM_MEDIA_TYPE,
          new io.swagger.v3.oas.models.media.MediaType());
    }
    if (requestBody.getContent().get(SwaggerConst.FORM_MEDIA_TYPE).getSchema() == null) {
      requestBody.getContent().get(SwaggerConst.FORM_MEDIA_TYPE)
          .setSchema(new MapSchema());
    }
    requestBody.getContent().get(SwaggerConst.FORM_MEDIA_TYPE)
        .getSchema().addProperty(getParameterName(formParam), schema);
  }
}
