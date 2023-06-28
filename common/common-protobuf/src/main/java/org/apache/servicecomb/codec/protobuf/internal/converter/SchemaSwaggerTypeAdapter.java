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

public class SchemaSwaggerTypeAdapter implements SwaggerTypeAdapter {
  private final Schema<?> schema;

  public SchemaSwaggerTypeAdapter(Schema<?> schema) {
    this.schema = schema;
  }

  @Override
  public String getRefType() {
    return schema.get$ref();
  }

  @Override
  public Schema<?> getArrayItem() {
    return schema.getItems();
  }

  @Override
  public Schema<?> getMapItem() {
    return schema.getAdditionalItems();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<String> getEnum() {
    return (List<String>) schema.getEnum();
  }

  @Override
  public String getType() {
    return schema.getType();
  }

  @Override
  public String getFormat() {
    return schema.getFormat();
  }

  @Override
  public boolean isJavaLangObject() {
    return "object".equals(getType());
  }
}
