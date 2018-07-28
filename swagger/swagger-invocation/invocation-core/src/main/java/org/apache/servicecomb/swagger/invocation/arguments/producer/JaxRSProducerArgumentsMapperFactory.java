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

package org.apache.servicecomb.swagger.invocation.arguments.producer;

import static org.apache.servicecomb.swagger.generator.jaxrs.processor.annotation.BeanParamAnnotationProcessor.SETTER_METHOD_PREFIX;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.BeanParam;

import org.apache.servicecomb.swagger.generator.jaxrs.processor.annotation.BeanParamAnnotationProcessor;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentsMapperConfig;
import org.apache.servicecomb.swagger.invocation.arguments.ProviderParameter;
import org.springframework.stereotype.Component;

import io.swagger.models.parameters.Parameter;

@Component
public class JaxRSProducerArgumentsMapperFactory extends ProducerArgumentsMapperFactory {
  @Override
  public boolean canProcess(ArgumentsMapperConfig config) {
    return config.getSwaggerGeneratorContext().getClass().getCanonicalName().equals(
        "org.apache.servicecomb.swagger.generator.jaxrs.JaxrsSwaggerGeneratorContext");
  }

  @Override
  protected Set<String> findAggregatedParamNames(Map<String, ProviderParameter> providerParamMap,
      Map<String, ParamWrapper<Parameter>> swaggerParamMap) {
    Set<String> aggregatedParamNames = new HashSet<>();
    for (Entry<String, ProviderParameter> providerParameterEntry : providerParamMap.entrySet()) {
      if (null == providerParameterEntry.getValue().getAnnotations()) {
        // request body may have no annotation
        continue;
      }
      for (Annotation annotation : providerParameterEntry.getValue().getAnnotations()) {
        if (annotation instanceof BeanParam) {
          aggregatedParamNames.add(providerParameterEntry.getKey());
        }
      }
    }

    return aggregatedParamNames;
  }

  @Override
  protected void generateAggregatedParamMapper(ArgumentsMapperConfig config,
      Map<String, ProviderParameter> providerParamMap, Map<String, ParamWrapper<Parameter>> swaggerParamMap,
      Set<String> aggregatedParamNames) {
    for (String aggregatedProducerParamName : aggregatedParamNames) {
      ProviderParameter aggregatedParam = providerParamMap.get(aggregatedProducerParamName);
      // producer param name -> swagger param name
      Map<String, String> producerToSwaggerParamNameMapper = getProducerToSwaggerParamNameMap(aggregatedParam);
      // producer param name -> swagger param index
      Map<String, Integer> producerNameToSwaggerIndexMap = new HashMap<>(producerToSwaggerParamNameMapper.size());
      for (Entry<String, String> producerSwaggerNameMapEntry : producerToSwaggerParamNameMapper.entrySet()) {
        producerNameToSwaggerIndexMap.put(
            producerSwaggerNameMapEntry.getKey(),
            swaggerParamMap.get(producerSwaggerNameMapEntry.getValue()).getIndex());
      }

      // create&add aggregated param mapper
      ArgumentMapper mapper = new ProducerBeanParamMapper(producerNameToSwaggerIndexMap,
          aggregatedParam.getIndex(),
          aggregatedParam.getType());
      config.addArgumentMapper(mapper);
    }
  }

  /**
   * <pre>
   * public class AggregatedParam {
   *   \@PathParam("pathSwaggerParam")
   *   private String pathProducerParam;
   *
   *   private String queryProducerParam;
   *
   *   \@QueryParam(value = "querySwaggerParam")
   *   public void setQueryProducerParam(String queryParam) {
   *     this.queryProducerParam = queryParam;
   *   }
   * }
   * </pre>
   * Given a BeanParam like above, will return a map like below:
   * {
   *   "pathProducerParam" -> "pathSwaggerParam",
   *   "queryProducerParam" -> "querySwaggerParam"
   * }
   */
  private Map<String, String> getProducerToSwaggerParamNameMap(ProviderParameter aggregatedParam) {
    Map<String, String> producerToSwaggerParamNameMapper = new HashMap<>();
    Class<?> aggregatedParamClazz = (Class<?>) aggregatedParam.getType();
    // map those params defined by BeanParam fields
    for (Field field : aggregatedParamClazz.getDeclaredFields()) {
      for (Annotation fieldAnnotation : field.getAnnotations()) {
        if (BeanParamAnnotationProcessor.SUPPORTED_PARAM_ANNOTATIONS.contains(fieldAnnotation.annotationType())) {
          producerToSwaggerParamNameMapper.put(
              field.getName(),
              retrieveVisibleParamName(fieldAnnotation));
          break;
        }
      }
    }
    // map those params defined by setter methods
    for (Method method : aggregatedParamClazz.getDeclaredMethods()) {
      final String methodName = method.getName();
      if (!methodName.startsWith(SETTER_METHOD_PREFIX)) {
        // only process setter methods
        continue;
      }
      // There should be one and only one param in a setter method
      for (Annotation setterAnnotation : method.getAnnotations()) {
        if (BeanParamAnnotationProcessor.SUPPORTED_PARAM_ANNOTATIONS.contains(setterAnnotation.annotationType())) {
          producerToSwaggerParamNameMapper.put(
              methodName.substring( // setParamName() -> "paramName"
                  SETTER_METHOD_PREFIX.length(), SETTER_METHOD_PREFIX.length() + 1).toLowerCase()
                  + methodName.substring(SETTER_METHOD_PREFIX.length() + 1),
              retrieveVisibleParamName(setterAnnotation));
          break;
        }
      }
    }
    return producerToSwaggerParamNameMapper;
  }
}
