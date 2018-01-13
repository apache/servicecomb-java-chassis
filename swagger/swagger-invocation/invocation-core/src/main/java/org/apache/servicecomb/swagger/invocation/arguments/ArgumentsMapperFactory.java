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

package org.apache.servicecomb.swagger.invocation.arguments;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.converter.Converter;
import org.apache.servicecomb.swagger.invocation.converter.ConverterMgr;
import org.apache.servicecomb.swagger.invocation.converter.impl.ConverterCommon;
import org.springframework.util.TypeUtils;

public abstract class ArgumentsMapperFactory {
  @Inject
  protected ConverterMgr converterMgr;

  protected InvocationType type;

  // key为ContextParameter
  private Map<Class<?>, ContextArgumentMapperFactory> factoryMap = new HashMap<>();

  public void setConverterMgr(ConverterMgr converterMgr) {
    this.converterMgr = converterMgr;
  }

  protected void createFactoryMap(List<ContextArgumentMapperFactory> factoryList) {
    factoryList.forEach(factory -> {
      factoryMap.put(factory.getContextClass(), factory);
    });
  }

  protected ContextArgumentMapperFactory findFactory(Type type) {
    if (type.getClass().equals(Class.class)) {
      return factoryMap.get((Class<?>) type);
    }
    return null;
  }

  public <T> T createArgumentsMapper(Method swaggerMethod, Method providerMethod) {
    ArgumentsMapperConfig config = new ArgumentsMapperConfig();
    config.setSwaggerMethod(swaggerMethod);
    config.setProviderMethod(providerMethod);

    collectArgumentsMapper(config);

    return createArgumentsMapper(config);
  }

  protected void collectArgumentsMapper(ArgumentsMapperConfig config) {
    List<ProviderParameter> providerNormalParams = collectContextArgumentsMapper(config);
    if (providerNormalParams.isEmpty()) {
      return;
    }

    if (isSwaggerWrapBody(config, providerNormalParams)) {
      collectWrapBodyMapper(config, providerNormalParams);
      return;
    }

    collectSwaggerArgumentsMapper(config, providerNormalParams);
  }

  protected boolean isSwaggerWrapBody(ArgumentsMapperConfig config, List<ProviderParameter> providerNormalParams) {
    Method swaggerMethod = config.getSwaggerMethod();
    if (swaggerMethod.getParameterCount() != 1) {
      return false;
    }

    Type swaggerType = config.getSwaggerMethod().getGenericParameterTypes()[0];
    if (!swaggerType.getClass().equals(Class.class)) {
      return false;
    }

    Type firstProviderParam = providerNormalParams.get(0).getType();
    if (TypeUtils.isAssignable(firstProviderParam, swaggerType)) {
      return false;
    }

    swaggerType = ((Class<?>) swaggerType).getFields()[0].getGenericType();
    Converter converter = converterMgr.findConverter(type, firstProviderParam, swaggerType);
    if (ConverterCommon.class.isInstance(converter)) {
      return false;
    }
    // 透明rpc的包装场景
    return true;
  }

  // 处理所有context类型的参数
  // 剩余的参数返回
  protected List<ProviderParameter> collectContextArgumentsMapper(ArgumentsMapperConfig config) {
    List<ProviderParameter> providerNormalParams = new ArrayList<>();

    Type[] providerParameterTypes = config.getProviderMethod().getGenericParameterTypes();
    for (int providerIdx = 0; providerIdx < providerParameterTypes.length; providerIdx++) {
      Type parameterType = providerParameterTypes[providerIdx];
      ContextArgumentMapperFactory factory = findFactory(parameterType);
      if (factory != null) {
        ArgumentMapper mapper = factory.create(providerIdx);
        config.addArgumentMapper(mapper);
        continue;
      }

      ProviderParameter pp = new ProviderParameter(providerIdx, parameterType);
      providerNormalParams.add(pp);
    }

    return providerNormalParams;
  }

  protected void collectSwaggerArgumentsMapper(ArgumentsMapperConfig config,
      List<ProviderParameter> providerNormalParams) {
    Method swaggerMethod = config.getSwaggerMethod();
    Type[] swaggerParams = swaggerMethod.getGenericParameterTypes();

    // 普通场景，要求除了provider上的context，其他参数必须按顺序一一对应，provider上的有效参数可以与契约不一致
    int minParamCount = Math.min(providerNormalParams.size(), swaggerParams.length);
    for (int swaggerIdx = 0; swaggerIdx < minParamCount; swaggerIdx++) {
      ProviderParameter providerParameter = providerNormalParams.get(swaggerIdx);
      Type swaggerParameter = swaggerParams[swaggerIdx];

      Converter converter = converterMgr.findConverter(type, providerParameter.getType(), swaggerParameter);
      ArgumentMapper mapper =
          createArgumentMapperWithConverter(swaggerIdx, providerParameter.getIndex(), converter);
      config.addArgumentMapper(mapper);
    }
  }

  protected void collectWrapBodyMapper(ArgumentsMapperConfig config, List<ProviderParameter> providerNormalParams) {
    // 将provider的参数存入唯一swagger参数的field
    // 或是将唯一swagger参数的field存入provider参数
    Method swaggerMethod = config.getSwaggerMethod();
    Class<?> swaggerParam = swaggerMethod.getParameterTypes()[0];
    Field[] swaggerParamFields = swaggerParam.getFields();

    // 普通场景，要求除了provider上的context，其他参数必须按顺序一一对应，provider上的有效参数可以与契约不一致
    int minParamCount = Math.min(providerNormalParams.size(), swaggerParamFields.length);

    Map<Integer, FieldInfo> fieldMap = new HashMap<>();
    for (int swaggerIdx = 0; swaggerIdx < minParamCount; swaggerIdx++) {
      ProviderParameter providerParameter = providerNormalParams.get(swaggerIdx);
      Field swaggerField = swaggerParamFields[swaggerIdx];
      swaggerField.setAccessible(true);

      Converter converter = converterMgr.findConverter(type,
          providerParameter.getType(),
          swaggerField.getGenericType());
      FieldInfo info = new FieldInfo(swaggerField, converter);
      fieldMap.put(providerParameter.getIndex(), info);
    }

    ArgumentMapper bodyFieldArg = createBodyFieldArgMapper(config, fieldMap);
    config.addArgumentMapper(bodyFieldArg);
  }

  protected abstract <T> T createArgumentsMapper(ArgumentsMapperConfig config);

  protected abstract ArgumentMapper createArgumentMapperWithConverter(int swaggerIdx, int providerIdx,
      Converter converter);

  protected abstract ArgumentMapper createBodyFieldArgMapper(ArgumentsMapperConfig config,
      Map<Integer, FieldInfo> fieldMap);
}
