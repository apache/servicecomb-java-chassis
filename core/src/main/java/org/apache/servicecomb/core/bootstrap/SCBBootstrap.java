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
package org.apache.servicecomb.core.bootstrap;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.client.LocalServiceRegistryClientImpl;
import org.apache.servicecomb.serviceregistry.registry.ServiceRegistryFactory;

public class SCBBootstrap {
  public SCBBootstrap useLocalRegistry() {
    return useLocalRegistry(System.getProperty(LocalServiceRegistryClientImpl.LOCAL_REGISTRY_FILE_KEY));
  }

  public SCBBootstrap useLocalRegistry(String localFile) {
    return useLocalRegistry(ServiceRegistryFactory.createLocal(localFile));
  }

  private SCBBootstrap useLocalRegistry(ServiceRegistry serviceRegistry) {
    serviceRegistry.init();
    RegistryUtils.setServiceRegistry(serviceRegistry);

    EventManager.eventBus = serviceRegistry.getEventBus();

    return this;
  }

  public static SCBEngine runWithSpring() {
    BeanUtils.init();
    return SCBEngine.getInstance();
  }

  public SCBEngine createSCBEngine() {
    return SCBEngine.getInstance();
  }

  public SCBEngine createSCBEngineForTest() {
    return new SCBEngineForTest();
  }
}
