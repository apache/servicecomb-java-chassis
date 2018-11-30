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

package org.apache.servicecomb.swagger.generator.springmvc.processor.parameter;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.swagger.generator.core.DefaultParameterProcessor;
import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.apache.servicecomb.swagger.generator.core.utils.ParamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.converter.ModelConverters;
import io.swagger.models.Model;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

/**
 * Flatten a object parameter into a set of flatten simple parameters.
 * Nesting object params and generic Object params are NOT supported.
 * We support query object just for aggregating query params instead of transporting objects in query param.
 * So we don't support a generic param whose generic type is complex, but a simple generic type param can be supported.
 */
public class SpringmvcDefaultObjectParameterProcessor implements DefaultParameterProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringmvcDefaultObjectParameterProcessor.class);

  @Override
  public void process(OperationGenerator operationGenerator, int paramIndex) {
    Model paramModel = getParamModel(operationGenerator, paramIndex);

    if (null == paramModel) {
      throw new Error(String.format("cannot find param, provider method is [%s], paramIndex = [%d]. "
              + "Please check your parameter definition.",
          operationGenerator.getProviderMethod().getName(), paramIndex));
    }

    LinkedHashMap<String, AbstractSerializableParameter<?>> resultParamMap = getFlattenParams(paramModel);

    addProviderParams(operationGenerator, resultParamMap);
  }

  private void addProviderParams(OperationGenerator operationGenerator,
      LinkedHashMap<String, AbstractSerializableParameter<?>> resultParamMap) {
    resultParamMap.forEach((paramName, param) -> operationGenerator.addProviderParameter(param));
  }

  private Model getParamModel(OperationGenerator operationGenerator, int paramIndex) {
    Type paramType = ParamUtils.getGenericParameterType(operationGenerator.getProviderMethod(), paramIndex);
    Property property = ModelConverters.getInstance().readAsProperty(paramType);
    // ensure type
    if (!RefProperty.class.isInstance(property)) {
      LOGGER.error("Unsupported property type: [{}], paramIndex is [{}]", property.getClass().getName(), paramIndex);
      return null;
    }

    Map<String, Model> models = ModelConverters.getInstance().readAll(paramType);

    // find param root
    RefProperty refProperty = (RefProperty) property;
    String refTypeName = refProperty.getSimpleRef();
    Model paramRoot = null;
    for (Entry<String, Model> entry : models.entrySet()) {
      if (refTypeName.equals(entry.getKey())) {
        paramRoot = entry.getValue();
        break;
      }
    }
    return paramRoot;
  }

  private LinkedHashMap<String, AbstractSerializableParameter<?>> getFlattenParams(Model paramModel) {
    LinkedHashMap<String, AbstractSerializableParameter<?>> flattenParamMap = new LinkedHashMap<>();
    // traversal the properties of current paramModel
    // create simple parameters, nesting object param is ignored
    for (Entry<String, Property> propertyEntry : paramModel.getProperties().entrySet()) {
      if (ParamUtils.isComplexProperty(propertyEntry.getValue())) {
        throw new Error(
            "A nesting complex field is found in the query object and this is not supported, field name  = ["
                + propertyEntry.getKey() + "]. Please remove this field or tag @JsonIgnore on it.");
      }
      AbstractSerializableParameter<?> newParameter = createSimpleParam(propertyEntry);
      flattenParamMap.put(propertyEntry.getKey(), newParameter);
    }

    return flattenParamMap;
  }

  private AbstractSerializableParameter<?> createSimpleParam(Entry<String, Property> propertyEntry) {
    AbstractSerializableParameter<?> newParameter = new QueryParameter();
    newParameter.setName(propertyEntry.getKey());
    newParameter.setProperty(propertyEntry.getValue());
    return newParameter;
  }
}
