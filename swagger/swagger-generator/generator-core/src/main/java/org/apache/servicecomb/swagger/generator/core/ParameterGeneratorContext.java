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
package org.apache.servicecomb.swagger.generator.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.v3.oas.models.media.Schema;
import jakarta.ws.rs.core.MediaType;

public class ParameterGeneratorContext extends OperationGeneratorContext {
  private JavaType parameterType;

  private String parameterName;

  private HttpParameterType httpParameterType;

  private Boolean explode;

  private Boolean required;

  private Object defaultValue;

  private Boolean rawJson;

  private Schema<?> schema;

  public ParameterGeneratorContext(OperationGeneratorContext parent) {
    super(parent);
  }

  public JavaType getParameterType() {
    return parameterType;
  }

  public void setParameterType(JavaType parameterType) {
    this.parameterType = parameterType;
  }

  public String getParameterName() {
    return parameterName;
  }

  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  public Boolean getExplode() {
    return explode;
  }

  public void setExplode(Boolean explode) {
    this.explode = explode;
  }

  public Boolean getRequired() {
    return required;
  }

  public void setRequired(Boolean required) {
    this.required = required;
  }

  public Boolean getRawJson() {
    return rawJson;
  }

  public void setRawJson(Boolean rawJson) {
    this.rawJson = rawJson;
  }

  public Schema<?> getSchema() {
    return schema;
  }

  public void setSchema(Schema<?> schema) {
    this.schema = schema;
  }

  public HttpParameterType getHttpParameterType() {
    return httpParameterType;
  }

  public void setHttpParameterType(HttpParameterType httpParameterType) {
    this.httpParameterType = httpParameterType;
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(Object defaultValue) {
    this.defaultValue = defaultValue;
  }

  public void updateConsumes(boolean isForm, boolean isBinary) {
    List<String> removed = new ArrayList<>();
    if (httpParameterType == HttpParameterType.BODY) {
      if (isForm) {
        throw new IllegalArgumentException("Both form and body parameter not allowed.");
      }
      for (String media : supportedConsumes) {
        if (SUPPORTED_BODY_CONTENT_TYPE.contains(media)) {
          continue;
        }
        removed.add(media);
      }
    } else if (httpParameterType == HttpParameterType.FORM) {
      for (String media : supportedConsumes) {
        if (!SUPPORTED_FORM_CONTENT_TYPE.contains(media)) {
          removed.add(media);
          continue;
        }
        if (isBinary && supportedConsumes.contains(MediaType.APPLICATION_FORM_URLENCODED)) {
          removed.add(MediaType.APPLICATION_FORM_URLENCODED);
        }
        if (!isBinary && supportedConsumes.contains(MediaType.MULTIPART_FORM_DATA)) {
          removed.add(MediaType.MULTIPART_FORM_DATA);
        }
      }
    } else {
      supportedConsumes.clear();
    }
    supportedConsumes.removeAll(removed);
  }

  public boolean isForm() {
    return httpParameterType == HttpParameterType.FORM;
  }

  public boolean isBinary() {
    if ("string".equals(this.schema.getType()) && "binary".equals(this.schema.getFormat())) {
      return true;
    }

    return "array".equals(this.schema.getType()) &&
        "string".equals(this.schema.getItems().getType()) &&
        "binary".equals(this.schema.getItems().getFormat());
  }

  public boolean isObject() {
    return "object".equals(this.schema.getType());
  }
}
