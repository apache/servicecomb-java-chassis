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

import java.util.Collections;

import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.registry.StaticMicroservice;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.definition.MicroserviceDefinition;
import org.apache.servicecomb.serviceregistry.registry.AbstractServiceRegistry;
import org.apache.servicecomb.serviceregistry.version.Version;
import org.apache.servicecomb.serviceregistry.version.VersionUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

import mockit.Mock;
import mockit.MockUp;

public class StaticMicroserviceVersionsTest {

  private static final String APP_ID = "testAppId";

  private static final String THIS_SERVICE = "thisService";

  private static final String MICROSERVICE_NAME = "3rdPartyService";

  private static final String ENVIRONMENT = "production";

  private static final String SERVICE_ID_PREFIX = APP_ID + "-" + ENVIRONMENT + "-" + MICROSERVICE_NAME + "-";

  @BeforeClass
  public static void beforeClass() {
    RegistryUtils.setServiceRegistry(new AbstractServiceRegistry(null, null,
        MicroserviceDefinition.create(APP_ID, THIS_SERVICE)) {
      @Override
      protected ServiceRegistryClient createServiceRegistryClient() {
        return null;
      }
    });
    RegistryUtils.getMicroservice().setEnvironment(ENVIRONMENT);
  }

  @Test
  public void addInstanceBasic() {
    StaticMicroserviceVersions staticMicroserviceVersions = createStaticMicroserviceVersions();

    MicroserviceInstance instance = new MicroserviceInstance();
    String serviceVersion = "1.2.1";
    staticMicroserviceVersions.addInstances(serviceVersion, Collections.singletonList(instance));

    MicroserviceVersionRule versionRule = staticMicroserviceVersions.getOrCreateMicroserviceVersionRule(serviceVersion);
    MicroserviceVersion latestMicroserviceVersion = versionRule.getLatestMicroserviceVersion();
    Assert.assertEquals(new Version(serviceVersion), latestMicroserviceVersion.getVersion());
    Microservice latestMicroservice = latestMicroserviceVersion.getMicroservice();
    Assert.assertEquals(APP_ID, latestMicroservice.getAppId());
    Assert.assertEquals(SERVICE_ID_PREFIX + serviceVersion, latestMicroservice.getServiceId());
    Assert.assertEquals(MICROSERVICE_NAME, latestMicroservice.getServiceName());
    Assert.assertEquals(serviceVersion, latestMicroservice.getVersion());
    Assert.assertEquals(ENVIRONMENT, latestMicroservice.getEnvironment());
    Assert.assertEquals(TestServiceIntf.class, ((StaticMicroservice) latestMicroservice).getSchemaIntfCls());
    Assert.assertEquals(1, versionRule.getInstances().size());
    Assert.assertSame(instance, versionRule.getInstances().get(instance.getInstanceId()));
    Assert.assertEquals(SERVICE_ID_PREFIX + serviceVersion, instance.getServiceId());
    Assert.assertTrue(instance.getInstanceId().startsWith(SERVICE_ID_PREFIX + serviceVersion + "-"));
  }

  @Test
  public void addInstancesAndGetVersionRuleMultiTimes() {
    StaticMicroserviceVersions staticMicroserviceVersions = createStaticMicroserviceVersions();

    // add instance1 with version 1.1.0
    MicroserviceInstance instance1 = new MicroserviceInstance();
    staticMicroserviceVersions.addInstances("1.1.0", Collections.singletonList(instance1));

    MicroserviceVersionRule versionRule = staticMicroserviceVersions.getOrCreateMicroserviceVersionRule("0.0.0+");
    MicroserviceVersion latestMicroserviceVersion = versionRule.getLatestMicroserviceVersion();
    Assert.assertEquals(new Version("1.1.0"), latestMicroserviceVersion.getVersion());
    Assert.assertEquals(1, versionRule.getInstances().size());
    Assert.assertSame(instance1, versionRule.getInstances().get(instance1.getInstanceId()));

    // add instance2 with version 1.1.1
    MicroserviceInstance instance2 = new MicroserviceInstance();
    staticMicroserviceVersions.addInstances("1.1.1", Collections.singletonList(instance2));

    versionRule = staticMicroserviceVersions.getOrCreateMicroserviceVersionRule("0.0.0+");
    latestMicroserviceVersion = versionRule.getLatestMicroserviceVersion();
    Assert.assertEquals(new Version("1.1.1"), latestMicroserviceVersion.getVersion());
    Assert.assertEquals(2, versionRule.getInstances().size());
    Assert.assertSame(instance1, versionRule.getInstances().get(instance1.getInstanceId()));
    Assert.assertSame(instance2, versionRule.getInstances().get(instance2.getInstanceId()));

    // add instance3 with version 0.1.0
    MicroserviceInstance instance3 = new MicroserviceInstance();
    staticMicroserviceVersions.addInstances("0.1.0", Collections.singletonList(instance3));

    versionRule = staticMicroserviceVersions.getOrCreateMicroserviceVersionRule("0.0.0+");
    latestMicroserviceVersion = versionRule.getLatestMicroserviceVersion();
    Assert.assertEquals(new Version("1.1.1"), latestMicroserviceVersion.getVersion());
    Assert.assertEquals(3, versionRule.getInstances().size());
    Assert.assertSame(instance1, versionRule.getInstances().get(instance1.getInstanceId()));
    Assert.assertSame(instance2, versionRule.getInstances().get(instance2.getInstanceId()));
    Assert.assertSame(instance3, versionRule.getInstances().get(instance3.getInstanceId()));

    // instance3 should be filtered out
    versionRule = staticMicroserviceVersions.getOrCreateMicroserviceVersionRule("1.0.0+");
    latestMicroserviceVersion = versionRule.getLatestMicroserviceVersion();
    Assert.assertEquals(new Version("1.1.1"), latestMicroserviceVersion.getVersion());
    Assert.assertEquals(2, versionRule.getInstances().size());
    Assert.assertSame(instance1, versionRule.getInstances().get(instance1.getInstanceId()));
    Assert.assertSame(instance2, versionRule.getInstances().get(instance2.getInstanceId()));
    Assert.assertNull(versionRule.getInstances().get(instance3.getInstanceId()));
  }

  private StaticMicroserviceVersions createStaticMicroserviceVersions() {
    EventBus eventBus = new EventBus();
    AppManager appManager = new AppManager(eventBus);
    appManager.setStaticMicroserviceVersionFactory(microservice -> new MockUp<MicroserviceVersion>() {
      @Mock
      public Version getVersion() {
        return VersionUtils.getOrCreate(microservice.getVersion());
      }

      @Mock
      public Microservice getMicroservice() {
        return microservice;
      }
    }.getMockInstance());
    return new StaticMicroserviceVersions(
        appManager, APP_ID, MICROSERVICE_NAME, TestServiceIntf.class);
  }

  private interface TestServiceIntf {
  }
}