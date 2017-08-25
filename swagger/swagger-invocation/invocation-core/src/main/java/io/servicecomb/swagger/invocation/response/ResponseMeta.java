/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.swagger.invocation.response;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JavaType;

import io.servicecomb.swagger.converter.ConverterMgr;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;

public class ResponseMeta {
  /**
   * swagger中定义的statusCode与java类型的映射，方便consumer端transport将码流转换为具体的类型
   */
  private JavaType javaType;

  private Map<String, JavaType> headers = new HashMap<>();

  public void init(ClassLoader classLoader, String packageName, Swagger swagger, Response response) {
    if (javaType == null) {
      Property property = response.getSchema();
      javaType = ConverterMgr.findJavaType(classLoader, packageName, swagger, property);
    }

    if (response.getHeaders() == null) {
      return;
    }
    for (Entry<String, Property> entry : response.getHeaders().entrySet()) {
      JavaType headerJavaType = ConverterMgr.findJavaType(classLoader, packageName, swagger, entry.getValue());
      headers.put(entry.getKey(), headerJavaType);
    }
  }

  public JavaType getJavaType() {
    return javaType;
  }

  public void setJavaType(JavaType javaType) {
    this.javaType = javaType;
  }

  public Map<String, JavaType> getHeaders() {
    return headers;
  }
}
