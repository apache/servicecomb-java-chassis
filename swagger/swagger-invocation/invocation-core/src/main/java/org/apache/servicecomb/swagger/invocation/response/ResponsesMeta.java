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
package org.apache.servicecomb.swagger.invocation.response;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.swagger.invocation.context.HttpStatus;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.Swagger;

public class ResponsesMeta {
  private static final JavaType COMMON_EXCEPTION_JAVATYPE = SimpleType.constructUnsafe(CommonExceptionData.class);

  private Map<Integer, ResponseMeta> responseMap = new HashMap<>();

  private ResponseMeta defaultResponse;

  // 最后一个参数returnType用于兼容场景
  // 历史版本中swagger中定义的return可能没定义class名，此时consumer与swagger接口是一致的
  // 如果不传return类型进来，完全以swagger为标准，会导致生成的class不等于return
  public void init(ClassLoader classLoader, String packageName, Swagger swagger, Operation operation,
      Type returnType) {
    initSuccessResponse(returnType);

    for (Entry<String, Response> entry : operation.getResponses().entrySet()) {
      if ("default".equals(entry.getKey())) {
        defaultResponse = new ResponseMeta();
        defaultResponse.init(classLoader, packageName, swagger, entry.getValue());
        continue;
      }

      Integer statusCode = Integer.parseInt(entry.getKey());
      ResponseMeta responseMeta = responseMap.computeIfAbsent(statusCode, k -> new ResponseMeta());
      responseMeta.init(classLoader, packageName, swagger, entry.getValue());
    }

    if (defaultResponse == null) {
      // swagger中没有定义default，加上default专用于处理exception
      ResponseMeta responseMeta = new ResponseMeta();
      responseMeta.setJavaType(COMMON_EXCEPTION_JAVATYPE);

      defaultResponse = responseMeta;
    }
  }

  protected void initSuccessResponse(Type returnType) {
    ResponseMeta successResponse = new ResponseMeta();
    successResponse.setJavaType(TypeFactory.defaultInstance().constructType(returnType));
    responseMap.put(Status.OK.getStatusCode(), successResponse);
  }

  public ResponseMeta findResponseMeta(int statusCode) {
    ResponseMeta responseMeta = responseMap.get(statusCode);
    if (responseMeta == null) {
      if (HttpStatus.isSuccess(statusCode)) {
        return responseMap.get(Status.OK.getStatusCode());
      }

      return defaultResponse;
    }

    return responseMeta;
  }
}
