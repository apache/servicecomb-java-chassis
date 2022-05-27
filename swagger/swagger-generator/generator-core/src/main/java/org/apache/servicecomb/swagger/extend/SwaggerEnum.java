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
package org.apache.servicecomb.swagger.extend;

import static org.apache.servicecomb.foundation.common.utils.StringBuilderUtils.appendLine;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.base.DynamicEnum;
import org.apache.servicecomb.foundation.common.base.EnumUtils;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import io.swagger.util.Json;

public enum SwaggerEnum {
  JDK {
    @SuppressWarnings({"unchecked", "deprecation"})
    @Override
    protected <T> T readEnumValue(Field enumField) {
      Enum<?> enumValue = EnumUtils.readEnum(enumField);
      return (T) Json.mapper().getSerializationConfig().getAnnotationIntrospector().findEnumValue(enumValue);
    }
  },
  DYNAMIC {
    @Override
    protected <T> T readEnumValue(Field enumField) {
      DynamicEnum<T> enumValue = EnumUtils.readEnum(enumField);
      return enumValue.getValue();
    }
  };

  public String findPropertyDescription(Class<?> enumClass, Annotation[] annotations) {
    StringBuilder sb = new StringBuilder();

    String propertyDescription = readDescription(annotations, null);
    if (StringUtils.isNotEmpty(propertyDescription)) {
      appendLine(sb, propertyDescription);
    }

    EnumUtils.findEnumFields(enumClass).forEach(enumField -> {
      Object enumValue = readEnumValue(enumField);
      String description = readDescription(enumField.getAnnotations(), "");
      appendLine(sb, "- %s: %s", enumValue, description);
    });

    return sb.toString();
  }

  public <T> List<T> readEnumValues(Class<?> enumClass) {
    return EnumUtils.findEnumFields(enumClass)
        .map(this::<T>readEnumValue)
        .collect(Collectors.toList());
  }

  protected abstract <T> T readEnumValue(Field enumField);

  @SuppressWarnings("unchecked")
  private <T extends Annotation> T findAnnotation(Annotation[] annotations, Class<T> cls) {
    if (annotations == null) {
      return null;
    }

    return Arrays.stream(annotations)
        .filter(annotation -> cls.isAssignableFrom(annotation.getClass()))
        .map(annotation -> (T) annotation)
        .findAny()
        .orElse(null);
  }

  private String readDescription(Annotation[] annotations, String defaultDescription) {
    ApiModelProperty apiModelProperty = findAnnotation(annotations, ApiModelProperty.class);
    if (apiModelProperty != null && StringUtils.isNotEmpty(apiModelProperty.value())) {
      return apiModelProperty.value();
    }

    ApiParam apiParam = findAnnotation(annotations, ApiParam.class);
    if (apiParam != null && StringUtils.isNotEmpty(apiParam.value())) {
      return apiParam.value();
    }

    return defaultDescription;
  }
}
