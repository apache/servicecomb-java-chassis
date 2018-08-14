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

package org.apache.servicecomb.serviceregistry.consumer;

import java.util.Arrays;

import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.InstanceCache;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class TestMicroserviceVersionRule {
  MicroserviceVersionRule microserviceVersionRule = new MicroserviceVersionRule("appId", "msName", "1+");

  @Test
  public void getVersionRule() {
    Assert.assertEquals("1.0.0+", microserviceVersionRule.getVersionRule().getVersionRule());
  }

  @Test
  public void addMicroserviceVersionNotMatch() {
    MicroserviceVersion microserviceVersion = MicroserviceVersionTestUtils.createMicroserviceVersion("1", "0.0.1");
    microserviceVersionRule.addMicroserviceVersion(microserviceVersion);

    Assert.assertNull(microserviceVersionRule.getLatestMicroserviceVersion());
  }

  @Test
  public void addMicroserviceVersionMatch() {
    MicroserviceVersion v2 = MicroserviceVersionTestUtils.createMicroserviceVersion("2", "2.0.0");
    microserviceVersionRule.addMicroserviceVersion(v2);
    Assert.assertSame(v2, microserviceVersionRule.getLatestMicroserviceVersion());

    MicroserviceVersion v1 = MicroserviceVersionTestUtils.createMicroserviceVersion("1", "1.0.0");
    microserviceVersionRule.addMicroserviceVersion(v1);
    Assert.assertSame(v2, microserviceVersionRule.getLatestMicroserviceVersion());
  }

  @Test
  public void deleteMicroserviceVersionNotMatch() {
    MicroserviceVersion v2 = MicroserviceVersionTestUtils.createMicroserviceVersion("2", "2.0.0");
    microserviceVersionRule.addMicroserviceVersion(v2);

    MicroserviceVersion v1 = MicroserviceVersionTestUtils.createMicroserviceVersion("1", "0.0.1");
    microserviceVersionRule.deleteMicroserviceVersion(v1);
    Assert.assertSame(v2, microserviceVersionRule.getLatestMicroserviceVersion());
  }

  @Test
  public void deleteMicroserviceVersionMatchNotExist() {
    MicroserviceVersion v2 = MicroserviceVersionTestUtils.createMicroserviceVersion("2", "2.0.0");
    microserviceVersionRule.addMicroserviceVersion(v2);

    MicroserviceVersion v1 = MicroserviceVersionTestUtils.createMicroserviceVersion("1", "1.0.0");
    microserviceVersionRule.deleteMicroserviceVersion(v1);
    Assert.assertSame(v2, microserviceVersionRule.getLatestMicroserviceVersion());
  }

  @Test
  public void deleteMicroserviceVersionMatchAndExist() {
    MicroserviceVersion v2 = MicroserviceVersionTestUtils.createMicroserviceVersion("2", "2.0.0");
    microserviceVersionRule.addMicroserviceVersion(v2);

    microserviceVersionRule.deleteMicroserviceVersion(v2);
    // keep a latest version always, event no versions.
    Assert.assertEquals(microserviceVersionRule.getLatestMicroserviceVersion(), v2);
  }

  @Test
  public void setInstances() {
    MicroserviceVersion v1 = MicroserviceVersionTestUtils.createMicroserviceVersion("1", "0.0.1");
    microserviceVersionRule.addMicroserviceVersion(v1);

    MicroserviceVersion v2 = MicroserviceVersionTestUtils.createMicroserviceVersion("2", "2.0.0");
    microserviceVersionRule.addMicroserviceVersion(v2);

    MicroserviceInstance instance1 = new MicroserviceInstance();
    instance1.setServiceId("1");
    instance1.setInstanceId("i1");

    MicroserviceInstance instance2 = new MicroserviceInstance();
    instance2.setServiceId("2");
    instance2.setInstanceId("i2");

    MicroserviceInstance instance3 = new MicroserviceInstance();
    instance3.setServiceId("3");
    instance3.setInstanceId("i3");

    InstanceCache orgCache = microserviceVersionRule.getInstanceCache();
    microserviceVersionRule.setInstances(Arrays.asList(instance1, instance2, instance3));

    Assert.assertThat(microserviceVersionRule.getInstances().values(), Matchers.contains(instance2));
    Assert.assertNotSame(orgCache, microserviceVersionRule.getInstanceCache());
    Assert.assertSame(microserviceVersionRule.getInstances(),
        microserviceVersionRule.getInstanceCache().getInstanceMap());
    Assert.assertSame(microserviceVersionRule.getInstances(),
        microserviceVersionRule.getVersionedCache().data());
    Assert.assertEquals(microserviceVersionRule.getLatestMicroserviceVersion(), v2);
    microserviceVersionRule.setInstances(Arrays.asList(instance2));
    Assert.assertEquals(microserviceVersionRule.getLatestMicroserviceVersion(), v2);

    MicroserviceVersion v3 = MicroserviceVersionTestUtils.createMicroserviceVersion("3", "3.0.0");
    microserviceVersionRule.addMicroserviceVersion(v3);
    Assert.assertEquals(microserviceVersionRule.getLatestMicroserviceVersion(), v2);
  }
}
