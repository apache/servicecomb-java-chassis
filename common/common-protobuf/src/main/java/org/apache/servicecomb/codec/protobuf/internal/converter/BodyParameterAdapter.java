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

import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;

public class BodyParameterAdapter implements SwaggerTypeAdapter {
  private final Model model;

  public BodyParameterAdapter(BodyParameter parameter) {
    this.model = parameter.getSchema();
  }

  @Override
  public String getRefType() {
    if (model instanceof RefModel) {
      return ((RefModel) model).getSimpleRef();
    }

    return null;
  }

  @Override
  public Property getArrayItem() {
    if (model instanceof ArrayModel) {
      return ((ArrayModel) model).getItems();
    }

    return null;
  }

  @Override
  public Property getMapItem() {
    if (model instanceof ModelImpl) {
      return ((ModelImpl) model).getAdditionalProperties();
    }

    return null;
  }

  @Override
  public List<String> getEnum() {
    if (model instanceof ModelImpl) {
      return ((ModelImpl) model).getEnum();
    }

    return null;
  }

  @Override
  public String getType() {
    if (model instanceof ModelImpl) {
      return ((ModelImpl) model).getType();
    }

    return null;
  }

  @Override
  public String getFormat() {
    if (model instanceof ModelImpl) {
      return ((ModelImpl) model).getFormat();
    }

    return null;
  }

  @Override
  public boolean isObject() {
    if (model instanceof ModelImpl) {
      ModelImpl modelImpl = (ModelImpl) model;
      return (ObjectProperty.TYPE.equals(modelImpl.getType())
          && modelImpl.getProperties() == null
          && modelImpl.getName() == null);
    }

    return false;
  }
}
