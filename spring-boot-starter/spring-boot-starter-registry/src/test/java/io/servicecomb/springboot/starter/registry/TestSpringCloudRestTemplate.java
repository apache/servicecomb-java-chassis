
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
package io.servicecomb.springboot.starter.registry;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;

import com.google.common.eventbus.EventBus;
import com.netflix.loadbalancer.IRule;

import io.servicecomb.core.CseContext;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.MicroserviceVersionMetaFactory;
import io.servicecomb.core.definition.loader.SchemaListenerManager;
import io.servicecomb.core.definition.loader.SchemaLoader;
import io.servicecomb.core.definition.schema.ConsumerSchemaFactory;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.loadbalance.LoadBalancer;
import io.servicecomb.loadbalance.LoadbalanceHandler;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.ServiceRegistry;
import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import io.servicecomb.serviceregistry.definition.MicroserviceDefinition;
import io.servicecomb.serviceregistry.registry.LocalServiceRegistry;
import io.servicecomb.swagger.invocation.AsyncResponse;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Tested;

/**
 * Testing the spring cloud rest template.
 */
public class TestSpringCloudRestTemplate {
  String appId = "app";

  EventBus eventBus = new EventBus();

  ServiceRegistry serviceRegistry = new LocalServiceRegistry(eventBus, ServiceRegistryConfig.INSTANCE,
      MicroserviceDefinition.create(appId, "self"));

  @Mocked
  ApplicationContext applicationContext;

  ConsumerSchemaFactory consumerSchemaFactory = new ConsumerSchemaFactory();

  SchemaLoader schemaLoader = new SchemaLoader();

  @Tested
  private SpringCloudRestTemplate springCloudRestTemplate;

  @Mocked
  SchemaListenerManager schemaListenerManager;

  LoadbalanceHandler handler = new LoadbalanceHandler();

  IRule rule = Mockito.mock(IRule.class);

  List<String> results = new ArrayList<>();

  private LoadBalancer loadBalancer = new LoadBalancer("loadBalancerName", rule);

  AsyncResponse asyncResponse = Mockito.mock(AsyncResponse.class);

  @Before
  public void setUp() throws Exception {
    springCloudRestTemplate = new SpringCloudRestTemplate();
    serviceRegistry.init();
    BeanUtils.setContext(applicationContext);

    serviceRegistry.getAppManager().setMicroserviceVersionFactory(new MicroserviceVersionMetaFactory());

    consumerSchemaFactory.setSchemaLoader(schemaLoader);
    CseContext.getInstance().setConsumerSchemaFactory(consumerSchemaFactory);
    CseContext.getInstance().setSchemaListenerManager(schemaListenerManager);

    RegistryUtils.setServiceRegistry(serviceRegistry);
  }

  @After
  public void tearDown() throws Exception {
    springCloudRestTemplate = null;
    RegistryUtils.setServiceRegistry(null);
    CseContext.getInstance().setConsumerSchemaFactory(null);
    CseContext.getInstance().setSchemaListenerManager(null);
  }

  @Test
  public void testSpringCloudTransport1() throws Exception {
    try {
      URI uri = URI.create("cse://app:test/");
      Assert.assertEquals(HttpMethod.GET, springCloudRestTemplate.createRequest(uri, HttpMethod.GET).getMethod());
    } catch (IOException e) {
      Assert.assertNotNull(e);
    }
  }

  @Test
  public void testSpringCloudTransport2() throws Exception {
    new MockUp<LoadbalanceHandler>(handler) {
      @Mock
      LoadBalancer getOrCreateLoadBalancer(Invocation invocation) {
        return loadBalancer;
      }

      @Mock
      void send(Invocation invocation, AsyncResponse asyncResp, final LoadBalancer choosenLB) throws Exception {
        results.add("sendNotRetry");
        invocation.next(asyncResponse);
        Assert.assertEquals("rest", invocation.getRealTransportName());
        Assert.assertEquals("default", invocation.getSchemaId());
        Assert.assertEquals("default", invocation.getOperationName());
      }
    };

    try {
      URI uri = URI.create("cse://app:test/");
      springCloudRestTemplate.handlersMap.put(uri.getAuthority(), handler);
      Assert.assertEquals(HttpMethod.GET, springCloudRestTemplate.createRequest(uri, HttpMethod.GET).getMethod());
      Assert.assertThat(results, Matchers.contains("sendNotRetry"));
    } catch (IOException e) {
      Assert.assertNotNull(e);
    }
  }
}
