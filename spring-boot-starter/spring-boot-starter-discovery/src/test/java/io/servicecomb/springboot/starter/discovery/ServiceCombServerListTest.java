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

package io.servicecomb.springboot.starter.discovery;

import static com.seanyinx.github.unit.scaffolding.AssertUtils.expectFailing;
import static io.servicecomb.core.Const.DEFAULT_VERSION_RULE;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.seanyinx.github.unit.scaffolding.Randomness;

import io.servicecomb.core.provider.consumer.ConsumerProviderManager;
import io.servicecomb.core.provider.consumer.ReferenceConfig;

public class ServiceCombServerListTest {

  private final ReferenceConfig referenceConfig = mock(ReferenceConfig.class);

  private final ConsumerProviderManager manager = mock(ConsumerProviderManager.class);

  private final CseRoutesProperties properties = new CseRoutesProperties(manager);

  private final ServiceCombServerList serverList = new ServiceCombServerList(properties);

  private String serviceId = Randomness.uniquify("serviceId");

  @Before
  public void setUp() throws Exception {
    when(manager.getReferenceConfig(serviceId)).thenReturn(referenceConfig);
    when(referenceConfig.getMicroserviceVersionRule()).thenReturn(DEFAULT_VERSION_RULE);
  }

  @Test
  public void blowsUpWhenServerListNotInitialized() {
    try {
      serverList.getInitialListOfServers();
      expectFailing(ServiceCombDiscoveryException.class);
    } catch (ServiceCombDiscoveryException e) {
      Assert.assertThat(e.getMessage(), is("Service list is not initialized"));
    }
  }
}
