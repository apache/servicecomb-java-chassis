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
package org.apache.servicecomb.it.schema;

import org.apache.servicecomb.foundation.test.scaffolding.model.Color;
import org.apache.servicecomb.provider.pojo.RpcSchema;

import io.swagger.annotations.SwaggerDefinition;

@RpcSchema(schemaId = "dataTypePojo")
@SwaggerDefinition(basePath = "/v1/dataTypePojo")
public class DataTypePojoSchema {
  public int intBody(int input) {
    return input;
  }

  public int intAdd(int num1, int num2) {
    return num1 + num2;
  }

  public String stringBody(String input) {
    return input;
  }

  public String stringConcat(String str1, String str2) {
    return str1 + str2;
  }

  public double doubleBody(double input) {
    return input;
  }

  public double doubleAdd(double num1, double num2) {
    return num1 + num2;
  }

  public float floatBody(float input) {
    return input;
  }

  public float floatAdd(float num1, float num2) {
    return num1 + num2;
  }

  public Color enumBody(Color color) {
    return color;
  }
}
