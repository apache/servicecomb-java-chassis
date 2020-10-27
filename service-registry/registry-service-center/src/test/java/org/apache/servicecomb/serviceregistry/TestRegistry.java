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

package org.apache.servicecomb.serviceregistry;

import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.registry.LocalServiceRegistryFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestRegistry {
  private static final AbstractConfiguration inMemoryConfig = new ConcurrentMapConfiguration();

  @BeforeClass
  public static void initSetup() {
    AbstractConfiguration dynamicConfig = ConfigUtil.createDynamicConfig();
    ConcurrentCompositeConfiguration configuration = new ConcurrentCompositeConfiguration();
    configuration.addConfiguration(dynamicConfig);
    configuration.addConfiguration(inMemoryConfig);

    ConfigurationManager.install(configuration);
  }

  @AfterClass
  public static void classTeardown() {
    Deencapsulation.setField(ConfigurationManager.class, "instance", null);
    Deencapsulation.setField(ConfigurationManager.class, "customConfigurationInstalled", false);
    Deencapsulation.setField(DynamicPropertyFactory.class, "config", null);
    RegistryUtils.setServiceRegistry(null);
  }

  @Before
  public void setUp() throws Exception {
    inMemoryConfig.clear();
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testDelegate() {
    ServiceRegistry serviceRegistry = LocalServiceRegistryFactory.createLocal();
    serviceRegistry.init();
    RegistryUtils.init();
    serviceRegistry.run();

    RegistryUtils.setServiceRegistry(serviceRegistry);
    Assert.assertEquals(serviceRegistry, RegistryUtils.getServiceRegistry());

    Assert.assertEquals(serviceRegistry.getServiceRegistryClient(), RegistryUtils.getServiceRegistryClient());

    Microservice microservice = RegistryUtils.getMicroservice();
    Assert.assertEquals(serviceRegistry.getMicroservice(), microservice);
    Assert.assertEquals(serviceRegistry.getMicroserviceInstance(), RegistryUtils.getMicroserviceInstance());

    List<MicroserviceInstance> instanceList = RegistryUtils.findServiceInstance("default", "default", "0.0.1");
    Assert.assertEquals(1, instanceList.size());
    Assert.assertEquals(RegistryUtils.getMicroservice().getServiceId(), instanceList.get(0).getServiceId());

    instanceList = RegistryUtils.findServiceInstance("default", "notExists", "0.0.1");
    Assert.assertNull(instanceList);

    MicroserviceInstances microserviceInstances =
        RegistryUtils.findServiceInstances("default", "default", "0.0.1");
    List<MicroserviceInstance> instanceLists = microserviceInstances.getInstancesResponse().getInstances();
    Assert.assertEquals(1, instanceLists.size());
    Assert.assertEquals(RegistryUtils.getMicroservice().getServiceId(), instanceLists.get(0).getServiceId());

    Map<String, String> properties = new HashMap<>();
    properties.put("k", "v");
    RegistryUtils.updateInstanceProperties(properties);
    Assert.assertEquals(properties, RegistryUtils.getMicroserviceInstance().getProperties());

    Assert.assertEquals(microservice, RegistryUtils.getMicroservice(microservice.getServiceId()));

    Assert.assertEquals("default", RegistryUtils.getAppId());
  }

  @Test
  public void testRegistryUtilGetPublishAddress(@Mocked InetAddress ethAddress) {
    new Expectations(NetUtils.class) {
      {
        NetUtils.getHostAddress();
        result = "1.1.1.1";
      }
    };
    String address = RegistrationManager.getPublishAddress();
    Assert.assertEquals("1.1.1.1", address);

    new Expectations(DynamicPropertyFactory.getInstance()) {
      {
        DynamicPropertyFactory.getInstance().getStringProperty(RegistrationManager.PUBLISH_ADDRESS, "");
        result = new DynamicStringProperty(RegistrationManager.PUBLISH_ADDRESS, "") {
          public String get() {
            return "127.0.0.1";
          }
        };
      }
    };
    Assert.assertEquals("127.0.0.1", RegistrationManager.getPublishAddress());

    new Expectations(DynamicPropertyFactory.getInstance()) {
      {
        ethAddress.getHostAddress();
        result = "1.1.1.1";
        NetUtils.ensureGetInterfaceAddress("eth100");
        result = ethAddress;
        DynamicPropertyFactory.getInstance().getStringProperty(RegistrationManager.PUBLISH_ADDRESS, "");
        result = new DynamicStringProperty(RegistrationManager.PUBLISH_ADDRESS, "") {
          public String get() {
            return "{eth100}";
          }
        };
      }
    };
    Assert.assertEquals("1.1.1.1", RegistrationManager.getPublishAddress());
  }

  @Test
  public void testRegistryUtilGetHostName(@Mocked InetAddress ethAddress) {
    new Expectations(NetUtils.class) {
      {
        NetUtils.getHostName();
        result = "testHostName";
      }
    };
    String host = RegistrationManager.getPublishHostName();
    Assert.assertEquals("testHostName", host);

    inMemoryConfig.addProperty(RegistrationManager.PUBLISH_ADDRESS, "127.0.0.1");
    Assert.assertEquals("127.0.0.1", RegistrationManager.getPublishHostName());

    new Expectations(DynamicPropertyFactory.getInstance()) {
      {
        ethAddress.getHostName();
        result = "testHostName";
        NetUtils.ensureGetInterfaceAddress("eth100");
        result = ethAddress;
      }
    };
    inMemoryConfig.addProperty(RegistrationManager.PUBLISH_ADDRESS, "{eth100}");
    Assert.assertEquals("testHostName", RegistrationManager.getPublishHostName());
  }

  @Test
  public void testGetRealListenAddress() throws Exception {
    new Expectations(NetUtils.class) {
      {
        NetUtils.getHostAddress();
        result = "1.1.1.1";
      }
    };

    Assert.assertEquals("rest://172.0.0.0:8080", RegistrationManager.getPublishAddress("rest", "172.0.0.0:8080"));
    Assert.assertNull(RegistrationManager.getPublishAddress("rest", null));

    URI uri = new URI(RegistrationManager.getPublishAddress("rest", "0.0.0.0:8080"));
    Assert.assertEquals("1.1.1.1:8080", uri.getAuthority());

    new Expectations(DynamicPropertyFactory.getInstance()) {
      {
        DynamicPropertyFactory.getInstance().getStringProperty(RegistrationManager.PUBLISH_ADDRESS, "");
        result = new DynamicStringProperty(RegistrationManager.PUBLISH_ADDRESS, "") {
          public String get() {
            return "1.1.1.1";
          }
        };
      }
    };
    Assert.assertEquals("rest://1.1.1.1:8080", RegistrationManager.getPublishAddress("rest", "172.0.0.0:8080"));

    InetAddress ethAddress = Mockito.mock(InetAddress.class);
    Mockito.when(ethAddress.getHostAddress()).thenReturn("1.1.1.1");
    new Expectations(DynamicPropertyFactory.getInstance()) {
      {
        NetUtils.ensureGetInterfaceAddress("eth20");
        result = ethAddress;
        DynamicPropertyFactory.getInstance().getStringProperty(RegistrationManager.PUBLISH_ADDRESS, "");
        result = new DynamicStringProperty(RegistrationManager.PUBLISH_ADDRESS, "") {
          public String get() {
            return "{eth20}";
          }
        };
      }
    };
    String query = URLEncodedUtils.format(Collections.singletonList(new BasicNameValuePair("country", "中 国")),
        StandardCharsets.UTF_8.name());
    Assert.assertEquals("rest://1.1.1.1:8080?" + query,
        RegistrationManager.getPublishAddress("rest", "172.0.0.0:8080?" + query));
  }
}
