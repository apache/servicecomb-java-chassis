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

import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

public class PropertyAdapter implements SwaggerTypeAdapter {
  private final Property property;

  public PropertyAdapter(Property property) {
    this.property = property;
  }

  @Override
  public String getRefType() {
    if (property instanceof RefProperty) {
      return ((RefProperty) property).getSimpleRef();
    }

    return null;
  }

  @Override
  public Property getArrayItem() {
    if (property instanceof ArrayProperty) {
      return ((ArrayProperty) property).getItems();
    }

    return null;
  }

  @Override
  public Property getMapItem() {
    if (property instanceof MapProperty) {
      return ((MapProperty) property).getAdditionalProperties();
    }

    return null;
  }

  @Override
  public List<String> getEnum() {
    if (property instanceof StringProperty) {
      return ((StringProperty) property).getEnum();
    }

    return null;
  }

  @Override
  public String getType() {
    return property.getType();
  }

  @Override
  public String getFormat() {
    return property.getFormat();
  }

  @Override
  public boolean isObject() {
    return property instanceof ObjectProperty;
  }
}
