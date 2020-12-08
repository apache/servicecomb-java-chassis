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
package org.apache.servicecomb.it.extend.engine;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.servicecomb.provider.pojo.Invoker;
import org.apache.servicecomb.provider.pojo.PojoConsumerMetaRefresher;
import org.apache.servicecomb.provider.pojo.PojoInvocation;
import org.apache.servicecomb.provider.pojo.PojoInvocationCreator;

/**
 * allow set transport, that makes integration test easier
 */
public class ITInvoker extends Invoker {
  @SuppressWarnings("unchecked")
  public static <T> T createProxy(String microserviceName, String schemaId, String transport, Class<?> consumerIntf) {
    ITInvoker invoker = new ITInvoker(microserviceName, schemaId, transport, consumerIntf);
    return (T) Proxy.newProxyInstance(consumerIntf.getClassLoader(), new Class<?>[] {consumerIntf}, invoker);
  }

  class ITPojoInvocationCreator extends PojoInvocationCreator {
    @Override
    public PojoInvocation create(Method method, PojoConsumerMetaRefresher metaRefresher, Object[] args) {
      PojoInvocation invocation = super.create(method, metaRefresher, args);
      invocation.getReferenceConfig().setTransport(transport);
      return invocation;
    }
  }

  private final String transport;

  public ITInvoker(String microserviceName, String schemaId, String transport, Class<?> consumerIntf) {
    super(microserviceName, schemaId, consumerIntf);
    this.transport = transport;
  }

  @Override
  public PojoInvocationCreator createInvocationCreator() {
    return new ITPojoInvocationCreator();
  }
}
