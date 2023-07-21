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

package org.apache.servicecomb.swagger.extend.introspector;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.base.EnumUtils;
import org.apache.servicecomb.swagger.extend.SwaggerEnum;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.introspect.Annotated;

import io.swagger.v3.core.jackson.SwaggerAnnotationIntrospector;


public class JsonPropertyIntrospector extends SwaggerAnnotationIntrospector {
  private static final long serialVersionUID = 4157263023893695762L;

  @SuppressWarnings("deprecation")
  @Override
  public String findEnumValue(Enum<?> value) {
    try {
      JsonProperty annotation = value.getClass().getField(value.name()).getAnnotation(JsonProperty.class);
      if (null == annotation || StringUtils.isEmpty(annotation.value())) {
        return super.findEnumValue(value);
      }
      return annotation.value();
    } catch (NoSuchFieldException e) {
      return super.findEnumValue(value);
    }
  }

  @Override
  public String findPropertyDescription(Annotated annotated) {
    Class<?> enumClass = annotated.getRawType();
    if (enumClass.isEnum()) {
      return SwaggerEnum.JDK.findPropertyDescription(enumClass, annotated.getAnnotated().getAnnotations());
    }
    if (EnumUtils.isDynamicEnum(enumClass)) {
      return SwaggerEnum.DYNAMIC.findPropertyDescription(enumClass, annotated.getAnnotated().getAnnotations());
    }

    return super.findPropertyDescription(annotated);
  }
}
