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

package org.apache.servicecomb.swagger.converter.model;

import org.apache.servicecomb.swagger.converter.property.ArrayPropertyConverter;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.models.ArrayModel;
import io.swagger.models.Swagger;

public class ArrayModelConverter extends AbstractModelConverter {
  @Override
  public JavaType doConvert(ClassLoader classLoader, String packageName, Swagger swagger, Object model) {
    ArrayModel arrayModel = (ArrayModel) model;

    if (arrayModel.getItems() != null) {
      return ArrayPropertyConverter.findJavaType(classLoader,
          packageName,
          swagger,
          arrayModel.getItems(),
          false);
    }

    // don't know when will this happen.
    throw new Error("not support null array model items.");
  }
}
