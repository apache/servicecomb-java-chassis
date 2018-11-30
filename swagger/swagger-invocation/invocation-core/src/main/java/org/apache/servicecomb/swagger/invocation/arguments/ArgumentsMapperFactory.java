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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.apache.servicecomb.swagger.generator.core.utils.ParamUtils;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.converter.Converter;
import org.apache.servicecomb.swagger.invocation.converter.ConverterMgr;
import org.apache.servicecomb.swagger.invocation.converter.impl.ConverterCommon;
import org.springframework.util.TypeUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;


/**
 * @param <T> type of the generated ArgumentsMapper
 */
public abstract class ArgumentsMapperFactory<T> {
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

    // no public field, it's not rpc wrapper class
    if (((Class<?>) swaggerType).getFields().length == 0) {
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

    final Annotation[][] parameterAnnotations = config.getProviderMethod().getParameterAnnotations();
    Type[] providerParameterTypes = config.getProviderMethod().getGenericParameterTypes();
    for (int providerIdx = 0; providerIdx < providerParameterTypes.length; providerIdx++) {
      Type parameterType = providerParameterTypes[providerIdx];
      ContextArgumentMapperFactory factory = findFactory(parameterType);
      if (factory != null) {
        ArgumentMapper mapper = factory.create(providerIdx);
        config.addArgumentMapper(mapper);
        continue;
      }

      ProviderParameter pp = new ProviderParameter(providerIdx, parameterType,
          retrieveVisibleParamName(config.getProviderMethod(), providerIdx))
          .setAnnotations(parameterAnnotations[providerIdx]);
      providerNormalParams.add(pp);
    }

    return providerNormalParams;
  }

  /**
   * Try to get the swagger param name of the corresponding producer/consumer method param
   * @param method producer/consumer method
   * @param paramIndex index of the producer/consumer method
   * @return the param name specified by param annotations, or the param name defined in code
   */
  public static String retrieveVisibleParamName(Method method, int paramIndex) {
    final Annotation[] annotations = method.getParameterAnnotations()[paramIndex];
    String paramName = null;
    for (Annotation annotation : annotations) {
      paramName = retrieveVisibleParamName(annotation);
    }
    if (null == paramName) {
      paramName = ParamUtils.getParameterName(method, paramIndex);
    }
    return paramName;
  }

  public static String retrieveVisibleParamName(Annotation annotation) {
    if (CookieParam.class.isInstance(annotation)) {
      return ((CookieParam) annotation).value();
    }
    if (CookieValue.class.isInstance(annotation)) {
      return ((CookieValue) annotation).name();
    }
    if (FormParam.class.isInstance(annotation)) {
      return ((FormParam) annotation).value();
    }
    if (HeaderParam.class.isInstance(annotation)) {
      return ((HeaderParam) annotation).value();
    }
    if (PathParam.class.isInstance(annotation)) {
      return ((PathParam) annotation).value();
    }
    if (PathVariable.class.isInstance(annotation)) {
      return ((PathVariable) annotation).value();
    }
    if (QueryParam.class.isInstance(annotation)) {
      return ((QueryParam) annotation).value();
    }
    if (RequestAttribute.class.isInstance(annotation)) {
      return ((RequestAttribute) annotation).name();
    }
    if (RequestHeader.class.isInstance(annotation)) {
      return ((RequestHeader) annotation).name();
    }
    if (RequestParam.class.isInstance(annotation)) {
      return ((RequestParam) annotation).name();
    }
    if (RequestPart.class.isInstance(annotation)) {
      return ((RequestPart) annotation).name();
    }

    return null;
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

  public abstract T createArgumentsMapper(ArgumentsMapperConfig config);

  protected abstract ArgumentMapper createArgumentMapperWithConverter(int swaggerIdx, int providerIdx,
      Converter converter);

  protected abstract ArgumentMapper createBodyFieldArgMapper(ArgumentsMapperConfig config,
      Map<Integer, FieldInfo> fieldMap);
}
