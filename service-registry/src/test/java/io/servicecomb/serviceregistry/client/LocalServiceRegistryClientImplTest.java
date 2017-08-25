/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.serviceregistry.client;

import java.io.InputStream;
import java.util.List;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;

public class LocalServiceRegistryClientImplTest {

  InputStream is;

  @Before
  public void loadRegistryFile() {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    is = loader.getResourceAsStream("registry.yaml");
  }

  @Test
  public void testLoadRegistryFile() {
    LocalServiceRegistryClientImpl registryClient = new LocalServiceRegistryClientImpl(is);
    Assert.assertNotNull(registryClient);
    Assert.assertThat(registryClient.getAllMicroservices().size(), Is.is(1));
    List<MicroserviceInstance> m = registryClient.findServiceInstance("", "myapp", "springmvctest", "");
    Assert.assertEquals(1, m.size());
  }
}
