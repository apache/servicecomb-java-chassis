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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;

/**
 * <pre>
 * consumer: void add(QueryWrapper query)
 *           class QueryWrapper {
 *             int x;
 *             int y;
 *           }
 * contract; void add(int x, int y)
 * </pre>
 */
public final class ConsumerBeanParamMapper extends ConsumerArgumentMapper {
  private class FieldMeta {
    String swaggerArgumentName;

    Getter<Object, Object> getter;

    public FieldMeta(String swaggerArgumentName, Getter<Object, Object> getter) {
      this.swaggerArgumentName = swaggerArgumentName;
      this.getter = getter;
    }
  }

  protected String invocationArgumentName;

  private List<FieldMeta> fields = new ArrayList<>();

  public ConsumerBeanParamMapper(String invocationArgumentName) {
    this.invocationArgumentName = invocationArgumentName;
  }

  public void addField(String invocationArgumentName, Getter<Object, Object> getter) {
    fields.add(new FieldMeta(invocationArgumentName, getter));
  }

  @Override
  public void invocationArgumentToSwaggerArguments(SwaggerInvocation swaggerInvocation,
      Map<String, Object> swaggerArguments,
      Map<String, Object> invocationArguments) {
    Object consumerArgument = invocationArguments.get(invocationArgumentName);
    if (consumerArgument == null) {
      return;
    }
    for (FieldMeta fieldMeta : fields) {
      swaggerArguments.put(fieldMeta.swaggerArgumentName, fieldMeta.getter.get(consumerArgument));
    }
  }
}
