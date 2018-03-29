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

package org.apache.servicecomb.swagger.generator.core;

import java.util.List;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

@Component
public class CompositeSwaggerGeneratorContext implements EmbeddedValueResolverAware {
  private List<SwaggerGeneratorContext> contextList;

  public CompositeSwaggerGeneratorContext() {
    contextList = SPIServiceUtils.getSortedService(SwaggerGeneratorContext.class);
  }

  @Override
  public void setEmbeddedValueResolver(StringValueResolver resolver) {
    for (SwaggerGeneratorContext context : contextList) {
      if (EmbeddedValueResolverAware.class.isInstance(context)) {
        ((EmbeddedValueResolverAware) context).setEmbeddedValueResolver(resolver);
      }
    }
  }

  public List<SwaggerGeneratorContext> getContextList() {
    return contextList;
  }

  public SwaggerGeneratorContext selectContext(Class<?> cls) {
    for (SwaggerGeneratorContext context : contextList) {
      if (context.canProcess(cls)) {
        return context;
      }
    }

    throw new Error("impossible, must be bug.");
  }
}
