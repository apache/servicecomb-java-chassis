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

import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.collectParameterName;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

/**
 * <pre>
 *   1.common
 *     context type parameter is not swagger parameter:
 *       InvocationContext
 *       HttpServletRequest
 *
 *   2.same version
 *     1) direct map (most scenes)
 *       interface method:
 *         class AddParam {
 *           int x;
 *           int y;
 *         }
 *         int add(InvocationContext context, AddParam param)
 *       swagger parameters:
 *         param
 *
 *       interface method:
 *         int add(int x, int y)
 *       swagger parameters:
 *         x, y
 *    2) swagger only one POJO paramter, extract all field to method parameters (POJO dev mode)
 *      interface method:
 *        int add(int x, int y)
 *      swagger parameters:
 *        param
 *    3) wrap some simple continuously swagger parameters to POJO (springmvc query parameters)
 *      interface method:
 *        int add(String name, AddParam param, Body body)
 *      swagger parameters:
 *        name, x, y, body
 *    4) wrap some simple and complex continuously swagger parameters to POJO  (JaxRS BeanParam)
 *      interface method:
 *        class BeanWrapper {
 *          int x;
 *          int y;
 *          Body body;
 *        }
 *        int add(String name, AddParam param, Body body)
 *      swagger parameters:
 *        name, x, y, body
 *  2. invoke old version
 *    interface method:
 *      int add(int x, int y, int z)
 *    swagger parameter:
 *      x, y
 *  3. invoke new version
 *    interface method:
 *      int add(int x, int y)
 *    swagger parameter:
 *      x, y, z
 * </pre>
 */
public abstract class AbstractArgumentsMapperCreator {
  protected SerializationConfig serializationConfig;

  // key is context class
  protected Map<Class<?>, ContextArgumentMapperFactory> contextFactorys;

  // consumer or producer
  protected Method providerMethod;

  protected SwaggerOperation swaggerOperation;

  protected List<ArgumentMapper> mappers = new ArrayList<>();

  protected List<Parameter> swaggerParameters;

  // body index in swagger parameters
  protected int swaggerBodyIdx;

  protected BodyParameter bodyParameter;

  protected Map<String, Property> swaggerBodyProperties;

  public AbstractArgumentsMapperCreator(SerializationConfig serializationConfig,
      Map<Class<?>, ContextArgumentMapperFactory> contextFactorys,
      Method providerMethod, SwaggerOperation swaggerOperation) {
    this.serializationConfig = serializationConfig;
    this.contextFactorys = contextFactorys;
    this.providerMethod = providerMethod;
    this.swaggerOperation = swaggerOperation;

    this.swaggerParameters = new ArrayList<>(this.swaggerOperation.getOperation().getParameters());

    bodyParameter = findSwaggerBodyParameter();
    swaggerBodyProperties = SwaggerUtils.getBodyProperties(swaggerOperation.getSwagger(), bodyParameter);
  }

  private BodyParameter findSwaggerBodyParameter() {
    for (int idx = 0; idx < swaggerParameters.size(); idx++) {
      Parameter parameter = swaggerParameters.get(idx);
      if (parameter instanceof BodyParameter) {
        swaggerBodyIdx = idx;
        return (BodyParameter) parameter;
      }
    }

    return null;
  }

  protected Integer findAndClearSwaggerParameterIndex(String name) {
    for (int idx = 0; idx < swaggerParameters.size(); idx++) {
      Parameter parameter = swaggerParameters.get(idx);
      if (parameter != null && name.equals(parameter.getName())) {
        swaggerParameters.set(idx, null);
        return idx;
      }
    }

    return null;
  }

  protected void doCreateArgumentsMapper() {
    java.lang.reflect.Parameter[] providerParameters = providerMethod.getParameters();
    for (int providerParamIdx = 0; providerParamIdx < providerParameters.length; providerParamIdx++) {
      java.lang.reflect.Parameter providerParameter = providerParameters[providerParamIdx];
      if (processContextParameter(providerParamIdx, providerParameter)) {
        continue;
      }

      String parameterName = collectParameterName(providerParameter);
      if (processKnownParameter(providerParamIdx, providerParameter, parameterName)) {
        continue;
      }

      if (processSwaggerBodyField(providerParamIdx, providerParameter, parameterName)) {
        continue;
      }

      JavaType providerType = TypeFactory.defaultInstance().constructType(providerParameter.getParameterizedType());
      if (SwaggerUtils.isBean(providerType)) {
        processBeanParameter(providerParamIdx, providerParameter);
        continue;
      }

      processUnknownParameter(parameterName);
    }
  }

  /**
   *
   * @param providerParamIdx
   * @param providerParameter processing provider parameter
   * @return true means processed
   */
  protected boolean processContextParameter(int providerParamIdx, java.lang.reflect.Parameter providerParameter) {
    ContextArgumentMapperFactory contextFactory = contextFactorys.get(providerParameter.getType());
    if (contextFactory == null) {
      return false;
    }

    mappers.add(contextFactory.create(providerParamIdx));
    return true;
  }

  /**
   *
   * @param providerParamIdx
   * @param providerParameter processing provider parameter
   * @param parameterName
   * @return true means processed
   */
  protected boolean processKnownParameter(int providerParamIdx, java.lang.reflect.Parameter providerParameter,
      String parameterName) {
    Integer swaggerIdx = findAndClearSwaggerParameterIndex(parameterName);
    if (swaggerIdx == null) {
      return false;
    }

    // complex scenes
    // swagger: int add(Body x)
    // producer: int add(int x, int y)
    if (bodyParameter != null &&
        !SwaggerUtils.isBean(providerParameter.getType()) &&
        swaggerIdx == swaggerBodyIdx &&
        SwaggerUtils.isBean(bodyParameter.getSchema())) {
      swaggerParameters.set(swaggerIdx, bodyParameter);
      return false;
    }

    ArgumentMapper mapper = createKnownParameterMapper(providerParamIdx, swaggerIdx);
    mappers.add(mapper);
    return true;
  }

  protected abstract ArgumentMapper createKnownParameterMapper(int providerParamIdx, Integer swaggerIdx);

  protected boolean processSwaggerBodyField(int providerParamIdx, java.lang.reflect.Parameter providerParameter,
      String parameterName) {
    if (swaggerBodyProperties == null) {
      return false;
    }

    Property property = swaggerBodyProperties.get(parameterName);
    if (property == null) {
      return false;
    }

    ArgumentMapper mapper = createSwaggerBodyFieldMapper(providerParamIdx, parameterName, swaggerBodyIdx);
    mappers.add(mapper);
    return true;
  }

  protected abstract ArgumentMapper createSwaggerBodyFieldMapper(int providerParamIdx, String parameterName,
      int swaggerBodyIdx);

  /**
   *
   * @param providerParamIdx
   * @param providerParameter processing provider parameter
   */
  protected abstract void processBeanParameter(int providerParamIdx, java.lang.reflect.Parameter providerParameter);

  protected abstract void processUnknownParameter(String parameterName);
}
