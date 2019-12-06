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
package org.apache.servicecomb.metrics.core;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.junit.Before;
import org.junit.Test;

public class TestMetricsBootListener {
  @Before
  public void setUp() {
    // Metrics boot depends on executor bean. Old test will fail but event handling not throw the error.
    BeanUtils.init();
  }

  @Test
  public void registerSchemas() {
    new SCBBootstrap().useLocalRegistry().createSCBEngineForTest().run();

    Microservice microservice = RegistryUtils.getMicroservice();
    microservice.getSchemas().contains("healthEndpoint");
    microservice.getSchemaMap().containsKey("healthEndpoint");

    microservice.getSchemas().contains("metricsEndpoint");
    microservice.getSchemaMap().containsKey("metricsEndpoint");

    SCBEngine.getInstance().destroy();
  }
}
