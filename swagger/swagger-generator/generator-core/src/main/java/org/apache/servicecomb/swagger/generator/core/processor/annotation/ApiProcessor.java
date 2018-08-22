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

package org.apache.servicecomb.swagger.generator.core.processor.annotation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.servicecomb.swagger.generator.core.ClassAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.springframework.util.StringUtils;

import io.swagger.annotations.Api;
import io.swagger.models.Swagger;

public class ApiProcessor implements ClassAnnotationProcessor {
  @Override
  public void process(Object annotation, SwaggerGenerator swaggerGenerator) {
    Api api = (Api) annotation;

    setTags(api, swaggerGenerator);
    // except for @Api, @RequestMapping can also set consumes and produces
    // @RequestMapping takes HIGHER priority than @Api for legacy reason
    processConsumes(api, swaggerGenerator.getSwagger());
    processProduces(api, swaggerGenerator.getSwagger());
  }

  private void processProduces(Api api, Swagger swagger) {
    List<String> validProducesList = getValidStringList(api.produces());
    if (isBlank(swagger.getProduces()) && !validProducesList.isEmpty()) {
      swagger.setProduces(validProducesList);
    }
  }

  private void processConsumes(Api api, Swagger swagger) {
    List<String> validConsumesList = getValidStringList(api.consumes());
    if (isBlank(swagger.getConsumes()) && !validConsumesList.isEmpty()) {
      swagger.setConsumes(validConsumesList);
    }
  }

  /**
   * Split {@link Api#consumes} and {@link Api#produces}, and filter empty items.
   */
  private List<String> getValidStringList(String rawString) {
    return Stream.of(rawString.split(","))
        .filter(s -> !StringUtils.isEmpty(s))
        .map(String::trim)
        .collect(Collectors.toList());
  }

  /**
   * @return true if {@code stringList} is empty or each element of {@code stringList} is empty;
   * otherwise false.
   */
  private boolean isBlank(List<String> stringList) {
    boolean isEmpty = null == stringList || stringList.isEmpty();
    if (isEmpty) {
      return true;
    }

    for (String s : stringList) {
      if (StringUtils.isEmpty(s)) {
        return true;
      }
    }
    return false;
  }

  private void setTags(Api api, SwaggerGenerator swaggerGenerator) {
    String[] tags = api.tags();
    for (String tagName : tags) {
      if (StringUtils.isEmpty(tagName)) {
        continue;
      }
      swaggerGenerator.addDefaultTag(tagName);
    }
  }
}
