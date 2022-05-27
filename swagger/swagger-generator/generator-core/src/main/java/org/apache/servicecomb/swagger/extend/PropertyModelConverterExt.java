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

import java.util.List;
import java.util.stream.Collectors;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.utils.PropertyModelConverter;

public class PropertyModelConverterExt extends PropertyModelConverter {
  public static Model toModel(Property property) {
    return new PropertyModelConverterExt().propertyToModel(property);
  }

  public static Property toProperty(Model model) {
    return new PropertyModelConverterExt().modelToProperty(model);
  }

  @Override
  public Model propertyToModel(Property property) {
    Model model = super.propertyToModel(property);
    copyNumberEnumToModel(property, model);
    return model;
  }

  public void copyNumberEnumToModel(Property property, Model model) {
    if (!(property instanceof IntegerProperty) || !(model instanceof ModelImpl)) {
      return;
    }

    List<Integer> intEnum = ((IntegerProperty) property).getEnum();
    if (intEnum == null) {
      return;
    }

    List<String> _enum = intEnum.stream()
        .map(value -> value.toString())
        .collect(Collectors.toList());
    ((ModelImpl) model).setEnum(_enum);
  }
}
