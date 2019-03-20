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

package org.apache.servicecomb.swagger.invocation.arguments.consumer;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Part;

import org.apache.commons.lang3.ClassUtils;
import org.apache.servicecomb.foundation.common.utils.LambdaMetafactoryUtils;
import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.engine.SwaggerOperation;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;
import org.apache.servicecomb.swagger.invocation.arguments.ContextArgumentMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.HttpMethod;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

/**
 * <pre>
 *   1.common
 *     context type parameter is not swagger parameter:
 *       InvocationContext
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
public class ConsumerArgumentsMapperCreator {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerArgumentsMapperCreator.class);

  private SerializationConfig serializationConfig;

  // key is context class
  private Map<Class<?>, ContextArgumentMapperFactory> contextFactorys;

  private Method consumerMethod;

  private SwaggerOperation swaggerOperation;

  private List<ArgumentMapper> mappers = new ArrayList<>();

  private java.lang.reflect.Parameter[] consumerParameters;

  private List<Parameter> swaggerParameters;

  private Map<String, Property> swaggerBodyProperties;

  // seems producer is pojo dev mode:
  // 1. http method is post
  // 2. only one parameter, it's a body parameter, and is bean type
  private boolean swaggerPojoDevLike;

  private int consumerParamIdx = 0;

  private int swaggerParamIdx = 0;

  private int unknownConsumerParams = 0;

  public ConsumerArgumentsMapperCreator(SerializationConfig serializationConfig,
      Map<Class<?>, ContextArgumentMapperFactory> contextFactorys,
      Method consumerMethod, SwaggerOperation swaggerOperation) {
    this.serializationConfig = serializationConfig;
    this.contextFactorys = contextFactorys;
    this.consumerMethod = consumerMethod;
    this.swaggerOperation = swaggerOperation;

    this.consumerParameters = consumerMethod.getParameters();
    this.swaggerParameters = this.swaggerOperation.getOperation().getParameters();

    BodyParameter bodyParameter = findSwaggerBodyParameter();
    swaggerBodyProperties = SwaggerUtils.getBodyProperties(swaggerOperation.getSwagger(), bodyParameter);
    swaggerPojoDevLike = swaggerOperation.getHttpMethod().equals(HttpMethod.POST)
        && swaggerParameters.size() == 1
        && swaggerBodyProperties != null;

    // ensure present parameter name
    if (consumerParameters.length != 0 && !consumerParameters[0].isNamePresent()) {
      String msg = String.format("parameter name is not present, method=%s\n"
              + "solution:\n"
              + "  change pom.xml, add compiler argument: -parameters, for example:\n"
              + "    <plugin>\n"
              + "      <groupId>org.apache.maven.plugins</groupId>\n"
              + "      <artifactId>maven-compiler-plugin</artifactId>\n"
              + "      <configuration>\n"
              + "        <compilerArgument>-parameters</compilerArgument>\n"
              + "      </configuration>\n"
              + "    </plugin>"
          , consumerMethod);
      throw new IllegalStateException(msg);
    }
  }

  private boolean isAllSameMapper() {
    for (ArgumentMapper mapper : mappers) {
      if (mapper instanceof ConsumerArgumentSame) {
        continue;
      }

      return false;
    }

    return true;
  }

  private boolean isBean(JavaType javaType) {
    if (javaType.isContainerType()) {
      return false;
    }

    Class<?> cls = javaType.getRawClass();
    if (ClassUtils.isPrimitiveOrWrapper(cls)) {
      return false;
    }

    if (cls == String.class
        || cls == Date.class
        || cls == LocalDate.class
        || cls == byte[].class
        || Part.class.isAssignableFrom(cls)) {
      return false;
    }

    return true;
  }

  private BodyParameter findSwaggerBodyParameter() {
    for (Parameter parameter : swaggerParameters) {
      if (parameter instanceof BodyParameter) {
        return (BodyParameter) parameter;
      }
    }

    return null;
  }

  private Integer findSwaggerParameterIndex(String name) {
    for (int idx = swaggerParamIdx; idx < swaggerParameters.size(); idx++) {
      if (name.equals(swaggerParameters.get(idx).getName())) {
        return idx;
      }
    }

    return null;
  }

  public ConsumerArgumentsMapper createArgumentsMapper() {
    doCreateArgumentsMapper();

    // if all mappers are SameMapper, then no need any mapper
    if (unknownConsumerParams == 0
        && mappers.size() == swaggerOperation.getOperation().getParameters().size()
        && isAllSameMapper()) {
      return new ArgumentsMapperDirectReuse();
    }

    return new ArgumentsMapperCommon(mappers, swaggerParameters.size());
  }

  private void doCreateArgumentsMapper() {
    for (consumerParamIdx = 0; consumerParamIdx < consumerParameters.length; consumerParamIdx++) {
      java.lang.reflect.Parameter consumerParameter = consumerParameters[consumerParamIdx];

      if (processContextParameter(consumerParameter)) {
        continue;
      }

      if (processKnownParameter(consumerParameter)) {
        continue;
      }

      if (swaggerPojoDevLike) {
        processToBodyField(consumerParameter);
        continue;
      }

      JavaType consumerType = TypeFactory.defaultInstance().constructType(consumerParameter.getParameterizedType());
      if (isBean(consumerType)) {
        processUnknownBeanParameter(consumerParameter);
        continue;
      }

      // real unknown parameter, new consumer invoke old producer, just ignore this parameter
      LOGGER.warn("new consumer invoke old version producer, parameter({}) is not exist in producer, method: {}",
          consumerParameter.getName(), consumerMethod);
      unknownConsumerParams++;
    }
  }

  /**
   *
   * @param consumerParameter processing consumer parameter
   * @return true means processed
   */
  private boolean processContextParameter(java.lang.reflect.Parameter consumerParameter) {
    ContextArgumentMapperFactory contextFactory = contextFactorys.get(consumerParameter.getType());
    if (contextFactory == null) {
      return false;
    }

    mappers.add(contextFactory.create(consumerParamIdx));
    return true;
  }

  /**
   *
   * @param consumerParameter processing consumer parameter
   * @return true means processed
   */
  private boolean processKnownParameter(java.lang.reflect.Parameter consumerParameter) {
    Integer swaggerIdx = findSwaggerParameterIndex(consumerParameter.getName());
    if (swaggerIdx == null) {
      return false;
    }

    mappers.add(new ConsumerArgumentSame(consumerParamIdx, swaggerIdx));
    swaggerParamIdx = swaggerIdx;
    return true;
  }

  private boolean processToBodyField(java.lang.reflect.Parameter consumerParameter) {
    // maybe this consumer parameter is a field of swagger body
    Property property = swaggerBodyProperties.get(consumerParameter.getName());
    if (property == null) {
      return false;
    }

    mappers.add(new ConsumerArgumentToBodyField(consumerParamIdx, consumerParameter.getName()));
    return true;
  }

  /**
   *
   * @param consumerParameter processing consumer parameter
   * @return true means processed
   */
  private void processUnknownBeanParameter(java.lang.reflect.Parameter consumerParameter) {
    ConsumerArgumentExtractConsumerFields mapper = new ConsumerArgumentExtractConsumerFields(consumerParamIdx);
    int maxSwaggerIdx = swaggerParamIdx;
    JavaType consumerType = TypeFactory.defaultInstance().constructType(consumerParameter.getParameterizedType());
    for (BeanPropertyDefinition propertyDefinition : serializationConfig.introspect(consumerType)
        .findProperties()) {
      Integer swaggerIdx = findSwaggerParameterIndex(propertyDefinition.getName());
      if (swaggerIdx == null) {
        // unknown field, ignore it
        continue;
      }

      maxSwaggerIdx = Math.max(maxSwaggerIdx, swaggerIdx);
      Getter<Object, Object> getter;
      if (propertyDefinition.hasGetter()) {
        getter = LambdaMetafactoryUtils.createLambda(propertyDefinition.getGetter().getAnnotated(), Getter.class);
      } else {
        getter = LambdaMetafactoryUtils.createGetter(propertyDefinition.getField().getAnnotated());
      }

      mapper.addField(swaggerIdx, getter);
    }
    mappers.add(mapper);
    swaggerParamIdx = maxSwaggerIdx;
  }
}
