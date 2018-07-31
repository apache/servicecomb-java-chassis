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

import java.lang.reflect.Proxy;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.provider.pojo.Invoker;

/**
 * allow set transport, that makes integration test easier
 */
public class ITInvoker extends Invoker {
  public static <T> T createProxy(String microserviceName, String schemaId, String transport, Class<?> consumerIntf) {
    ITInvoker invoker = new ITInvoker(microserviceName, schemaId, transport, consumerIntf);
    return invoker.getProxy();
  }

  private String transport;

  private Object proxy;

  public ITInvoker(String microserviceName, String schemaId, String transport, Class<?> consumerIntf) {
    super(microserviceName, schemaId, consumerIntf);
    this.transport = transport;
    this.proxy = Proxy.newProxyInstance(consumerIntf.getClassLoader(), new Class<?>[] {consumerIntf}, this);
  }

  public String getMicroserviceName() {
    return microserviceName;
  }

  public String getSchemaId() {
    return schemaId;
  }

  public String getTransport() {
    return transport;
  }

  @SuppressWarnings("unchecked")
  public <T> T getProxy() {
    return (T) proxy;
  }

  @Override
  protected ReferenceConfig findReferenceConfig() {
    ReferenceConfig referenceConfig = CseContext.getInstance()
        .getConsumerProviderManager()
        .createReferenceConfig(microserviceName);
    referenceConfig.setTransport(transport);
    return referenceConfig;
  }
}
