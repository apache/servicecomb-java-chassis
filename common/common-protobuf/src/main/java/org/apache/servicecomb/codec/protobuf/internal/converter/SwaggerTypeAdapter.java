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
package org.apache.servicecomb.codec.protobuf.internal.converter;

import java.util.List;

import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.Property;

public interface SwaggerTypeAdapter {
  static SwaggerTypeAdapter create(Object swaggerType) {
    if (swaggerType instanceof Property) {
      return new PropertyAdapter((Property) swaggerType);
    }

    if (swaggerType instanceof SerializableParameter) {
      return new SerializableParameterAdapter((SerializableParameter) swaggerType);
    }

    if (swaggerType instanceof BodyParameter) {
      return new BodyParameterAdapter((BodyParameter) swaggerType);
    }

    throw new IllegalStateException("not support swagger type: " + swaggerType.getClass().getName());
  }

  String getRefType();

  Property getArrayItem();

  Property getMapItem();

  List<String> getEnum();

  String getType();

  String getFormat();

  boolean isObject();
}
