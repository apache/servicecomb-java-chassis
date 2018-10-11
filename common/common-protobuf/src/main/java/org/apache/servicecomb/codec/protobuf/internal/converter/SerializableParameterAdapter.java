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

import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.Property;

public class SerializableParameterAdapter implements SwaggerTypeAdapter {
  private final SerializableParameter parameter;

  public SerializableParameterAdapter(SerializableParameter parameter) {
    this.parameter = parameter;
  }

  @Override
  public String getRefType() {
    return null;
  }

  @Override
  public Property getArrayItem() {
    if ("array".equals(parameter.getType())) {
      return parameter.getItems();
    }
    
    return null;
  }

  @Override
  public Property getMapItem() {
    return null;
  }

  @Override
  public List<String> getEnum() {
    return parameter.getEnum();
  }

  @Override
  public String getType() {
    return parameter.getType();
  }

  @Override
  public String getFormat() {
    return parameter.getFormat();
  }

  @Override
  public boolean isObject() {
    return false;
  }
}
