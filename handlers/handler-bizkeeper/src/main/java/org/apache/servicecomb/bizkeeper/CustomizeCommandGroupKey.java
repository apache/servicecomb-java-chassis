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

package org.apache.servicecomb.bizkeeper;

import org.apache.servicecomb.core.Invocation;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixKey;
import com.netflix.hystrix.util.InternMap;

public class CustomizeCommandGroupKey extends HystrixKey.HystrixKeyDefault implements HystrixCommandGroupKey {

  private String invocationType;

  private String microserviceName;

  private String schema;

  private String operation;

  public CustomizeCommandGroupKey(Invocation invocation) {
    super(invocation.getInvocationType().name() + "." + invocation.getOperationMeta().getMicroserviceQualifiedName());
    this.invocationType = invocation.getInvocationType().name();
    this.microserviceName = invocation.getMicroserviceName();
    this.schema = invocation.getSchemaId();
    this.operation = invocation.getOperationName();
  }

  private static final InternMap<Invocation, CustomizeCommandGroupKey> intern =
      new InternMap<Invocation, CustomizeCommandGroupKey>(
          new InternMap.ValueConstructor<Invocation, CustomizeCommandGroupKey>() {
            @Override
            public CustomizeCommandGroupKey create(Invocation invocation) {
              return new CustomizeCommandGroupKey(invocation);
            }
          });


  public static HystrixCommandGroupKey asKey(String type, Invocation invocation) {
    return intern.interned(invocation);
  }

  public String getInvocationType() {
    return invocationType;
  }

  public String getMicroserviceName() {
    return microserviceName;
  }

  public String getOperation() {
    return operation;
  }

  public String getSchema() {
    return schema;
  }
}
