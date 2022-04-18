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

import static io.swagger.util.ReflectionUtils.isVoid;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.swagger.converter.ConverterMgr;
import org.apache.servicecomb.swagger.invocation.context.HttpStatus;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;

import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.Swagger;

/**
 * <pre>
 * two Scenes:
 * 1.consumer interface + swagger
 *   interface declare success response type
 *   and can declare exceptions response type by annotations
 *   consumer interface meta never changed and has high priority
 *
 *   so, merge them to be one ResponsesMeta
 *
 * 2.restTemplate + swagger
 *   can only declare success response type
 *   and not stable
 *
 *   so, will wrap swagger meta
 *
 *   note:
 *   old version support: List&lt;User&gt; users = restTemplate.postForObject(...., List.class)
 *     in fact, in this time, type is determined by swagger meta
 *   new version:
 *     1) if request response type is List/Set/Map, and there is element type defined, then use swagger type,
 *     2) other times use request response type
 *     3) compare to old version, add support of ParameterizedTypeReference
 * </pre>
 */
public class ResponsesMeta {
  private static final JavaType COMMON_EXCEPTION_JAVA_TYPE = SimpleType.constructUnsafe(CommonExceptionData.class);

  private static final JavaType OBJECT_JAVA_TYPE = SimpleType.constructUnsafe(Object.class);

  private static final ResponseMetaMapper GLOBAL_DEFAULT_MAPPER = SPIServiceUtils
      .getPriorityHighestService(ResponseMetaMapper.class);

  private final Map<Integer, JavaType> responseMap = new HashMap<>();

  private JavaType defaultResponse;

  public void init(Swagger swagger, Operation operation) {
    if (responseMap.isEmpty()) {
      responseMap.put(Status.OK.getStatusCode(), OBJECT_JAVA_TYPE);
      initGlobalDefaultMapper();
    }

    for (Entry<String, Response> entry : operation.getResponses().entrySet()) {
      JavaType javaType = ConverterMgr.findJavaType(swagger, entry.getValue().getResponseSchema());

      if ("default".equals(entry.getKey())) {
        defaultResponse = javaType;
        continue;
      }

      Integer statusCode = Integer.parseInt(entry.getKey());
      JavaType existing = responseMap.get(statusCode);
      if (existing == null || !isVoid(javaType)) {
        responseMap.put(statusCode, javaType);
      }
    }

    responseMap.putIfAbsent(ExceptionFactory.CONSUMER_INNER_STATUS_CODE, COMMON_EXCEPTION_JAVA_TYPE);
    responseMap.putIfAbsent(ExceptionFactory.PRODUCER_INNER_STATUS_CODE, COMMON_EXCEPTION_JAVA_TYPE);
    responseMap.putIfAbsent(Status.TOO_MANY_REQUESTS.getStatusCode(), COMMON_EXCEPTION_JAVA_TYPE);
    responseMap.putIfAbsent(Status.REQUEST_TIMEOUT.getStatusCode(), COMMON_EXCEPTION_JAVA_TYPE);
    responseMap.putIfAbsent(Status.SERVICE_UNAVAILABLE.getStatusCode(), COMMON_EXCEPTION_JAVA_TYPE);

    if (defaultResponse == null) {
      // swagger中没有定义default，加上default专用于处理exception
      defaultResponse = OBJECT_JAVA_TYPE;
    }
  }

  public void cloneTo(ResponsesMeta target) {
    target.defaultResponse = defaultResponse;
    target.responseMap.putAll(responseMap);
  }

  protected void initGlobalDefaultMapper() {
    if (GLOBAL_DEFAULT_MAPPER != null) {
      Map<Integer, JavaType> mappers = GLOBAL_DEFAULT_MAPPER.getMapper();
      if (mappers != null) {
        responseMap.putAll(mappers);
      }
    }
  }

  public Map<Integer, JavaType> getResponseMap() {
    return responseMap;
  }

  public JavaType findResponseType(int statusCode) {
    JavaType responseType = responseMap.get(statusCode);
    if (responseType == null) {
      if (HttpStatus.isSuccess(statusCode)) {
        return responseMap.get(Status.OK.getStatusCode());
      }

      return defaultResponse;
    }

    return responseType;
  }

  public void setResponseType(int statusCode, JavaType javaType) {
    this.responseMap.put(statusCode, javaType);
  }
}
