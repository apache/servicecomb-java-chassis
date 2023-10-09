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

import java.util.Collections;
import java.util.List;

import org.apache.servicecomb.config.MicroserviceProperties;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.executor.ExecutorManager;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.discovery.TelnetInstancePing;
import org.springframework.core.env.Environment;

public class SCBBootstrap {
  public static SCBEngine createSCBEngineForTest(Environment environment) {
    RegistrationManager registrationManager = new RegistrationManager(Collections.emptyList());
    DiscoveryManager discoveryManager = new DiscoveryManager(Collections.emptyList(),
        List.of(new TelnetInstancePing()));
    registrationManager.init();
    discoveryManager.init();
    MicroserviceProperties microserviceProperties = new MicroserviceProperties();
    microserviceProperties.setApplication("test");
    microserviceProperties.setName("test");
    microserviceProperties.setVersion("0.0.1");

    SCBEngine result = new SCBEngineForTest(environment);
    result.setDiscoveryManager(discoveryManager);
    result.setRegistrationManager(registrationManager);
    result.setBootListeners(Collections.emptyList());
    result.setMicroserviceProperties(microserviceProperties);
    result.setBootUpInformationCollectors(Collections.emptyList());
    result.setExecutorManager(new ExecutorManager());
    result.setTransportManager(new TransportManager());
    return result;
  }
}
