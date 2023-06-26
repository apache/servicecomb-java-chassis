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

import io.swagger.v3.oas.models.media.Schema;

public interface SwaggerTypeAdapter {
  static SwaggerTypeAdapter create(Object swaggerType) {
    if (swaggerType instanceof SwaggerTypeAdapter) {
      return (SwaggerTypeAdapter) swaggerType;
    }

    throw new IllegalStateException("not support swagger type: " + swaggerType.getClass().getName());
  }

  String getRefType();

  Schema getArrayItem();

  Schema getMapItem();

  List<String> getEnum();

  String getType();

  String getFormat();

  boolean isJavaLangObject();
}
