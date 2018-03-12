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

package org.apache.servicecomb.serviceregistry.api.registry;

import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import mockit.Deencapsulation;

public class TestFrameworkVersions {
  private static ServiceRegistryClient registryClient = Mockito.mock(ServiceRegistryClient.class);
  private static ServiceRegistry serviceRegistry = Mockito.mock(ServiceRegistry.class);
  private static ServiceCenterInfo serviceCenterInfo = Mockito.mock(ServiceCenterInfo.class);

  @BeforeClass
  public static void init() {
    Deencapsulation.setField(RegistryUtils.class, "serviceRegistry", serviceRegistry);
    Mockito.when(serviceRegistry.getServiceRegistryClient()).thenReturn(registryClient);
    Mockito.when(registryClient.getServiceCenterInfo()).thenReturn(serviceCenterInfo);
    Mockito.when(serviceCenterInfo.getVersion()).thenReturn("1.0.0");
  }

  @AfterClass
  public static void teardown() {
    Deencapsulation.setField(RegistryUtils.class, "serviceRegistry", null);
  }

  @Test
  public void testFrameworkVersions() {
    Assert.assertEquals("ServiceComb:null", FrameworkVersions.allVersions());
  }
}
