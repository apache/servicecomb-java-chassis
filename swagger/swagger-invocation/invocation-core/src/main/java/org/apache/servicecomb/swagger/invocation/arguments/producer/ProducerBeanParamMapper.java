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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.bean.Setter;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;

public class ProducerBeanParamMapper extends ProducerArgumentMapper {
  private class FieldMeta {
    String swaggerParameterName;

    Setter<Object, Object> setter;

    public FieldMeta(String swaggerParameterName, Setter<Object, Object> setter) {
      this.swaggerParameterName = swaggerParameterName;
      this.setter = setter;
    }
  }

  protected String invocationArgumentName;

  private final Class<?> producerParamType;

  private List<FieldMeta> fields = new ArrayList<>();

  public ProducerBeanParamMapper(String invocationArgumentName, Class<?> producerParamType) {
    this.invocationArgumentName = invocationArgumentName;
    this.producerParamType = producerParamType;
  }

  public void addField(String swaggerParameterName, Setter<Object, Object> setter) {
    fields.add(new FieldMeta(swaggerParameterName, setter));
  }

  @Override
  public void swaggerArgumentToInvocationArguments(SwaggerInvocation invocation,
      Map<String, Object> swaggerArguments, Map<String, Object> invocationArguments) {
    try {
      Object paramInstance = producerParamType.newInstance();
      invocationArguments.put(invocationArgumentName, paramInstance);

      for (FieldMeta fieldMeta : fields) {
        Object value = swaggerArguments.get(fieldMeta.swaggerParameterName);
        if (value != null) {
          // can not set primitive data
          fieldMeta.setter.set(paramInstance, value);
        }
      }
    } catch (Throwable e) {
      throw new IllegalStateException("failed to map bean param.", e);
    }
  }
}
