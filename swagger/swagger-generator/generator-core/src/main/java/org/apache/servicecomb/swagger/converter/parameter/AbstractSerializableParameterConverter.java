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

package org.apache.servicecomb.swagger.converter.parameter;

import org.apache.servicecomb.swagger.converter.Converter;
import org.apache.servicecomb.swagger.converter.ConverterMgr;
import org.apache.servicecomb.swagger.converter.property.ArrayPropertyConverter;
import org.apache.servicecomb.swagger.converter.property.StringPropertyConverter;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.models.Swagger;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.StringProperty;

public class AbstractSerializableParameterConverter implements Converter {

  @Override
  public JavaType convert(ClassLoader classLoader, String packageName, Swagger swagger, Object def) {
    AbstractSerializableParameter<?> param = (AbstractSerializableParameter<?>) def;

    switch (param.getType()) {
      case ArrayProperty.TYPE:
        return ArrayPropertyConverter.findJavaType(classLoader,
            packageName,
            swagger,
            param.getItems(),
            param.isUniqueItems());
      case StringProperty.TYPE:
        return StringPropertyConverter.findJavaType(classLoader,
            packageName,
            swagger,
            param.getType(),
            param.getFormat(),
            param.getEnum());
      default:
        return ConverterMgr.findJavaType(param.getType(), param.getFormat());
    }
  }
}
