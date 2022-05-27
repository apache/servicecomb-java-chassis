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
package org.apache.servicecomb.core.definition;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.swagger.invocation.arguments.ArgumentsMapper;
import org.apache.servicecomb.swagger.invocation.response.ResponsesMeta;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * An InvocationRuntimeType indicates the associated java type information of this invocation.
 *
 * For producer, java type information NOT be changed for each invocation.
 *
 * For Consumer, java type information depend on method signature, or not available when in edge or
 * invoked by raw type way like RestTemplate or InvokerUtils.
 */
public class InvocationRuntimeType {
  private Class<?> associatedClass;

  private Method associatedMethod;

  private final ResponsesMeta responsesMeta;

  private ArgumentsMapper argumentsMapper;

  public InvocationRuntimeType(Class<?> associatedClass, Method associatedMethod, ResponsesMeta responsesMeta,
      ArgumentsMapper argumentsMapper) {
    this.associatedClass = associatedClass;
    this.associatedMethod = associatedMethod;
    this.argumentsMapper = argumentsMapper;
    this.responsesMeta = responsesMeta;
  }

  public InvocationRuntimeType(ResponsesMeta responsesMeta) {
    this.responsesMeta = responsesMeta;
  }

  public Class<?> getAssociatedClass() {
    return this.associatedClass;
  }

  public Method getAssociatedMethod() {
    return this.associatedMethod;
  }

  public ArgumentsMapper getArgumentsMapper() {
    return this.argumentsMapper;
  }

  public JavaType findResponseType(int statusCode) {
    return responsesMeta.findResponseType(statusCode);
  }

  public void setSuccessResponseType(JavaType javaType) {
    if (javaType != null) {
      // when javaType is null , using swagger type, do not override
      responsesMeta.setResponseType(Status.OK.getStatusCode(), javaType);
    }
  }

  public void setSuccessResponseType(Type type) {
    if (type != null) {
      // when javaType is null , using swagger type, do not override
      responsesMeta.setResponseType(Status.OK.getStatusCode(), TypeFactory.defaultInstance().constructType(type));
    }
  }

  public void setAssociatedClass(Class<?> associatedClass) {
    this.associatedClass = associatedClass;
  }

  public void setAssociatedMethod(Method associatedMethod) {
    this.associatedMethod = associatedMethod;
  }

  public void setArgumentsMapper(ArgumentsMapper argumentsMapper) {
    this.argumentsMapper = argumentsMapper;
  }

  public boolean isRawConsumer() {
    return this.associatedClass == null;
  }
}
