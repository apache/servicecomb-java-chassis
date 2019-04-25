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

import org.apache.servicecomb.foundation.common.utils.bean.Setter;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;

public class ProducerBeanParamMapper implements ArgumentMapper {
  private class FieldMeta {
    int swaggerIdx;

    Setter<Object, Object> setter;

    public FieldMeta(int swaggerIdx, Setter<Object, Object> setter) {
      this.swaggerIdx = swaggerIdx;
      this.setter = setter;
    }
  }

  private int producerIdx;

  private final Class<?> producerParamType;

  private List<FieldMeta> fields = new ArrayList<>();

  public ProducerBeanParamMapper(int producerIdx, Class<?> producerParamType) {
    this.producerIdx = producerIdx;
    this.producerParamType = producerParamType;
  }

  public void addField(int swaggerIdx, Setter<Object, Object> setter) {
    fields.add(new FieldMeta(swaggerIdx, setter));
  }

  @Override
  public void mapArgument(SwaggerInvocation invocation, Object[] producerArguments) {
    try {
      Object paramInstance = producerParamType.newInstance();
      producerArguments[producerIdx] = paramInstance;

      for (FieldMeta fieldMeta : fields) {
        Object value = invocation.getSwaggerArgument(fieldMeta.swaggerIdx);
        fieldMeta.setter.set(paramInstance, value);
      }
    } catch (Throwable e) {
      throw new IllegalStateException("failed to map bean param.", e);
    }
  }
}
