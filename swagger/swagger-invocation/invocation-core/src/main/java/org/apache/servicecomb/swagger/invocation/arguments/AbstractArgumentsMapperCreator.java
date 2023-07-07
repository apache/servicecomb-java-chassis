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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;

import com.fasterxml.jackson.databind.SerializationConfig;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

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
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractArgumentsMapperCreator {
  protected boolean isSwaggerBodyField = false;

  protected SerializationConfig serializationConfig;

  // key is context class
  protected Map<Class<?>, ContextArgumentMapperFactory> contextFactorys;

  // consumer or producer
  protected Method providerMethod;

  protected Class<?> providerClass;

  protected SwaggerOperation swaggerOperation;

  protected List<ArgumentMapper> mappers = new ArrayList<>();

  protected List<Parameter> swaggerParameters;

  protected RequestBody bodyParameter;

  // For pojo wrapped bodies only
  protected Map<String, Schema> swaggerBodyProperties;

  protected Set<String> processedSwaggerParameters;

  public AbstractArgumentsMapperCreator(SerializationConfig serializationConfig,
      Map<Class<?>, ContextArgumentMapperFactory> contextFactorys, Class<?> providerClass,
      Method providerMethod, SwaggerOperation swaggerOperation) {
    this.serializationConfig = serializationConfig;
    this.contextFactorys = contextFactorys;
    this.providerClass = providerClass;
    this.providerMethod = providerMethod;
    this.swaggerOperation = swaggerOperation;

    this.swaggerParameters = this.swaggerOperation.getOperation().getParameters();

    this.bodyParameter = this.swaggerOperation.getOperation().getRequestBody();
    this.swaggerBodyProperties = readSwaggerBodyProperties();
    this.processedSwaggerParameters = new HashSet<>();
  }

  private Map<String, Schema> readSwaggerBodyProperties() {
    if (bodyParameter == null || bodyParameter.getContent() == null
        || bodyParameter.getContent().size() == 0) {
      return null;
    }
    // For pojo wrapped bodies only
    if (bodyParameter.getContent().get(SwaggerConst.FILE_MEDIA_TYPE) != null ||
        bodyParameter.getContent().get(SwaggerConst.FORM_MEDIA_TYPE) != null) {
      return null;
    }
    Schema schema = bodyParameter.getContent().entrySet().iterator().next().getValue().getSchema();
    if (schema != null && schema.get$ref() != null) {
      schema = SwaggerUtils.getSchema(swaggerOperation.getSwagger(), schema.get$ref());
    }
    if (schema != null && schema.getProperties() != null) {
      return schema.getProperties();
    }
    return null;
  }

  protected void doCreateArgumentsMapper() {
    java.lang.reflect.Parameter[] providerParameters = providerMethod.getParameters();
    for (int providerParamIdx = 0; providerParamIdx < providerParameters.length; providerParamIdx++) {
      java.lang.reflect.Parameter providerParameter = providerParameters[providerParamIdx];
      if (processContextParameter(providerParameter)) {
        continue;
      }

      String parameterName = collectParameterName(providerParameter);
      if (processKnownParameter(providerParamIdx, parameterName)) {
        processedSwaggerParameters.add(parameterName);
        continue;
      }

      if (processSwaggerBodyField(providerParamIdx, parameterName)) {
        processedSwaggerParameters.add(parameterName);
        isSwaggerBodyField = true;
        continue;
      }

      if (processBeanParameter(providerParamIdx, providerParameter)) {
        continue;
      }

      processUnknownParameter(providerParamIdx, providerParameter, parameterName);
    }

    // Process swagger parameters that not in method parameters
    if (swaggerParameters != null) {
      for (Parameter parameter : swaggerParameters) {
        if (!processedSwaggerParameters.contains(parameter.getName())) {
          processPendingSwaggerParameter(parameter);
        }
      }
    }
    if (bodyParameter != null && bodyParameter.getExtensions() != null
        && bodyParameter.getExtensions().get(SwaggerConst.EXT_BODY_NAME) != null
        && !processedSwaggerParameters.contains(
        (String) bodyParameter.getExtensions().get(SwaggerConst.EXT_BODY_NAME))) {
      processPendingBodyParameter(bodyParameter);
    }
  }

  /**
   *
   * @param providerParameter processing provider parameter
   * @return true means processed
   */
  protected boolean processContextParameter(java.lang.reflect.Parameter providerParameter) {
    ContextArgumentMapperFactory contextFactory = contextFactorys.get(providerParameter.getType());
    if (contextFactory == null) {
      return false;
    }

    mappers.add(contextFactory
        .create(providerParameter.getName(), providerParameter.getName()));
    return true;
  }

  /**
   * Parameters has the same name in method and swagger.
   */
  protected boolean processKnownParameter(int providerParamIdx, String invocationArgumentName) {
    if (!parameterNameExistsInSwagger(invocationArgumentName)) {
      return false;
    }

    ArgumentMapper mapper = createKnownParameterMapper(providerParamIdx, invocationArgumentName);
    mappers.add(mapper);
    return true;
  }

  protected boolean parameterNameExistsInSwagger(String parameterName) {
    if (this.swaggerParameters != null) {
      for (Parameter parameter : this.swaggerParameters) {
        if (parameterName.equals(parameter.getName())) {
          return true;
        }
      }
    }
    if (this.bodyParameter != null && this.bodyParameter.getContent() != null) {
      if (this.bodyParameter.getContent().get(SwaggerConst.FORM_MEDIA_TYPE) != null &&
          this.bodyParameter.getContent().get(SwaggerConst.FORM_MEDIA_TYPE).getSchema() != null &&
          this.bodyParameter.getContent().get(SwaggerConst.FORM_MEDIA_TYPE).getSchema().getProperties() != null) {
        return this.bodyParameter.getContent()
            .get(SwaggerConst.FORM_MEDIA_TYPE).getSchema().getProperties().get(parameterName) != null;
      }
      if (this.bodyParameter.getContent().get(SwaggerConst.FILE_MEDIA_TYPE) != null &&
          this.bodyParameter.getContent().get(SwaggerConst.FILE_MEDIA_TYPE).getSchema() != null &&
          this.bodyParameter.getContent().get(SwaggerConst.FILE_MEDIA_TYPE).getSchema().getProperties() != null) {
        return this.bodyParameter.getContent()
            .get(SwaggerConst.FILE_MEDIA_TYPE).getSchema().getProperties().get(parameterName) != null;
      }
    }
    if (this.bodyParameter != null && this.bodyParameter.getExtensions() != null) {
      return parameterName.equals(this.bodyParameter.getExtensions().get(SwaggerConst.EXT_BODY_NAME));
    }
    return false;
  }

  protected abstract ArgumentMapper createKnownParameterMapper(int providerParamIdx, String parameterName);

  /**
   * Process POJO wrapped parameters, e.g.
   *   method(int foo, int bar)
   * and Form parameters, e.g.
   *   method(@FormParam("foo") int foo, @FormParam("bar") int bar)
   */
  protected boolean processSwaggerBodyField(int providerParamIdx, String parameterName) {
    if (swaggerBodyProperties == null || swaggerBodyProperties.get(parameterName) == null) {
      return false;
    }

    ArgumentMapper mapper = createSwaggerBodyFieldMapper(providerParamIdx, parameterName);
    mappers.add(mapper);
    return true;
  }

  protected abstract ArgumentMapper createSwaggerBodyFieldMapper(int providerParamIdx, String parameterName);

  /**
   * Bean parameters, e.g.
   *
   *   method(QueryModels queries)
   *
   * where swagger should be:
   *    - in: query
   *      name: foo
   *    - in: query
   *      name: bar
   */
  protected abstract boolean processBeanParameter(int providerParamIdx, java.lang.reflect.Parameter providerParameter);

  protected abstract void processUnknownParameter(int providerParamIdx, java.lang.reflect.Parameter providerParameter,
      String parameterName);

  /**
   * Process parameters that in swagger but not in method.
   */
  protected abstract void processPendingSwaggerParameter(Parameter parameter);

  /**
   * Process body parameter that in swagger but not in method.
   */
  protected abstract void processPendingBodyParameter(RequestBody parameter);
}
