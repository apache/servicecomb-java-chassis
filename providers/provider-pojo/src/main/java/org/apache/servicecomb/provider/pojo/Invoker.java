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

package org.apache.servicecomb.provider.pojo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.servicecomb.core.SCBEngine;

public class Invoker implements InvocationHandler {
  protected final PojoConsumerMetaRefresher metaRefresher;

  protected final PojoInvocationCreator invocationCreator;

  protected final DefaultMethodMeta defaultMethodMeta = new DefaultMethodMeta();

  protected InvocationCaller invocationCaller;

  @SuppressWarnings("unchecked")
  public static <T> T createProxy(String microserviceName, String schemaId, Class<?> consumerIntf) {
    Invoker invoker = new Invoker(microserviceName, schemaId, consumerIntf);
    return (T) Proxy.newProxyInstance(consumerIntf.getClassLoader(), new Class<?>[] {consumerIntf}, invoker);
  }

  public Invoker(String microserviceName, String schemaId, Class<?> consumerIntf) {
    this.metaRefresher = createInvokerMeta(microserviceName, schemaId, consumerIntf);
    this.invocationCreator = createInvocationCreator();
  }

  protected PojoConsumerMetaRefresher createInvokerMeta(String microserviceName, String schemaId,
      Class<?> consumerIntf) {
    return new PojoConsumerMetaRefresher(microserviceName, schemaId, consumerIntf);
  }

  public PojoInvocationCreator createInvocationCreator() {
    return new PojoInvocationCreator();
  }

  protected InvocationCaller createInvocationCaller() {
    if (SCBEngine.getInstance().isFilterChainEnabled()) {
      return new FilterInvocationCaller();
    }

    return new HandlerInvocationCaller();
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.isDefault()) {
      return defaultMethodMeta.getOrCreateMethodHandle(proxy, method)
          .invokeWithArguments(args);
    }
    SCBEngine.getInstance().ensureStatusUp();
    prepareInvocationCaller();
    return invocationCaller.call(method, metaRefresher, invocationCreator, args);
  }

  protected void prepareInvocationCaller() {
    if (invocationCaller != null) {
      return;
    }

    this.invocationCaller = createInvocationCaller();
  }
}
