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
package org.apache.servicecomb.swagger.invocation.validator;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.validation.ParameterNameProvider;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

public class DefaultParameterNameProvider implements ParameterNameProvider {
  private final Map<AccessibleObject, List<String>> methodCache = new ConcurrentHashMapEx<>();

  @Override
  public List<String> getParameterNames(Constructor<?> constructor) {
    return methodCache.computeIfAbsent(constructor, k -> getParameterNamesEx(constructor));
  }

  @Override
  public List<String> getParameterNames(Method method) {
    return methodCache.computeIfAbsent(method, k -> getParameterNamesEx(method));
  }

  private List<String> getParameterNamesEx(Executable methodOrConstructor) {
    Parameter[] parameters = methodOrConstructor.getParameters();
    List<String> parameterNames = new ArrayList<>(parameters.length);
    for (int idx = 0; idx < parameters.length; idx++) {
      Parameter parameter = parameters[idx];
      String parameterName = parameter.isNamePresent() ? parameter.getName() : "arg" + idx;
      parameterNames.add(parameterName);
    }
    return Collections.unmodifiableList(parameterNames);
  }
}
