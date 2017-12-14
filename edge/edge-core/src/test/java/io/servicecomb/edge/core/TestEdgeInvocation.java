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

package io.servicecomb.edge.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.common.rest.filter.HttpServerFilter;
import io.servicecomb.common.rest.locator.OperationLocator;
import io.servicecomb.common.rest.locator.ServicePathManager;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.MicroserviceVersionMeta;
import io.servicecomb.core.provider.consumer.ReactiveResponseExecutor;
import io.servicecomb.core.provider.consumer.ReferenceConfig;
import io.servicecomb.foundation.common.exceptions.ServiceCombException;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.ServiceRegistry;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.consumer.AppManager;
import io.servicecomb.serviceregistry.consumer.MicroserviceVersionRule;
import io.servicecomb.serviceregistry.definition.DefinitionConst;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.HttpServerRequestWrapperForTest;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestEdgeInvocation {
  String microserviceName = "ms";

  @Mocked
  RoutingContext context;

  @Mocked
  HttpServerRequestWrapperForTest request;

  List<HttpServerFilter> httpServerFilters = Collections.emptyList();

  MicroserviceMeta microserviceMeta = new MicroserviceMeta("app:ms");

  ReferenceConfig referenceConfig = new ReferenceConfig();

  EdgeInvocation edgeInvocation = new EdgeInvocation();

  HttpServletRequestEx requestEx;

  HttpServletResponseEx responseEx;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setup() {
    Deencapsulation.setField(referenceConfig, "microserviceMeta", microserviceMeta);
    referenceConfig.setMicroserviceVersionRule("latest");
    referenceConfig.setTransport("rest");

    edgeInvocation.init(microserviceName, context, "/base", httpServerFilters);

    requestEx = Deencapsulation.getField(edgeInvocation, "requestEx");
    responseEx = Deencapsulation.getField(edgeInvocation, "responseEx");
  }

  @Test
  public void edgeInvoke(@Mocked MicroserviceVersionMeta microserviceVersionMeta) {
    Map<String, Boolean> result = new LinkedHashMap<>();
    edgeInvocation = new EdgeInvocation() {
      @Override
      protected void findMicroserviceVersionMeta() {
        result.put("findMicroserviceVersionMeta", true);
      }

      @Override
      protected void findRestOperation(MicroserviceMeta microserviceMeta) {
        result.put("findRestOperation", true);
      }

      @Override
      protected void scheduleInvocation() {
        result.put("scheduleInvocation", true);
      }
    };
    edgeInvocation.latestMicroserviceVersionMeta = microserviceVersionMeta;

    edgeInvocation.edgeInvoke();

    Assert.assertTrue(result.get("findMicroserviceVersionMeta"));
    Assert.assertTrue(result.get("findRestOperation"));
    Assert.assertTrue(result.get("scheduleInvocation"));
  }

  @Test
  public void findMicroserviceVersionMetaNullLatestVersion(@Mocked AppManager appManager,
      @Mocked MicroserviceVersionRule microserviceVersionRule, @Mocked ServiceRegistry serviceRegistry) {
    String versionRule = DefinitionConst.VERSION_RULE_ALL;
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getServiceRegistry();
        result = serviceRegistry;
        serviceRegistry.getAppManager();
        result = appManager;
        RegistryUtils.getAppId();
        result = "app";
        appManager.getOrCreateMicroserviceVersionRule("app", microserviceName, versionRule);
        result = microserviceVersionRule;
        microserviceVersionRule.getLatestMicroserviceVersion();
        result = null;
      }
    };

    expectedException.expect(ServiceCombException.class);
    expectedException.expectMessage(Matchers
        .is("Failed to find latest MicroserviceVersionMeta, appId=app, microserviceName=ms, versionRule=0.0.0+."));

    edgeInvocation.findMicroserviceVersionMeta();
  }

  @Test
  public void findMicroserviceVersionMetaNormal(@Mocked AppManager appManager,
      @Mocked MicroserviceVersionRule microserviceVersionRule,
      @Mocked MicroserviceVersionMeta latestMicroserviceVersionMeta,
      @Mocked ServiceRegistry serviceRegistry) {
    String versionRule = DefinitionConst.VERSION_RULE_ALL;
    microserviceName = "app:ms";
    edgeInvocation.microserviceName = microserviceName;
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getServiceRegistry();
        result = serviceRegistry;
        serviceRegistry.getAppManager();
        result = appManager;
        RegistryUtils.getAppId();
        result = "app";
        appManager.getOrCreateMicroserviceVersionRule("app", microserviceName, versionRule);
        result = microserviceVersionRule;
        microserviceVersionRule.getLatestMicroserviceVersion();
        result = latestMicroserviceVersionMeta;
      }
    };

    edgeInvocation.findMicroserviceVersionMeta();

    Assert.assertSame(latestMicroserviceVersionMeta, edgeInvocation.latestMicroserviceVersionMeta);
  }

  @Test
  public void chooseVersionRule_default() {
    Assert.assertEquals(DefinitionConst.VERSION_RULE_ALL, edgeInvocation.chooseVersionRule());
  }

  @Test
  public void chooseVersionRule_set() {
    String versionRule = "1.0.0";
    edgeInvocation.setVersionRule(versionRule);

    Assert.assertEquals(versionRule, edgeInvocation.chooseVersionRule());
  }

  @Test
  public void locateOperation(@Mocked ServicePathManager servicePathManager,
      @Mocked OperationLocator operationLocator) {
    new Expectations() {
      {
        servicePathManager.consumerLocateOperation(anyString, anyString);
        result = operationLocator;
      }
    };

    Assert.assertSame(operationLocator, edgeInvocation.locateOperation(servicePathManager));
  }

  @Test
  public void createInvocation(@Mocked MicroserviceVersionMeta microserviceVersionMeta,
      @Mocked MicroserviceVersionRule microserviceVersionRule, @Mocked RestOperationMeta restOperationMeta,
      @Mocked Microservice microservice) {
    edgeInvocation.latestMicroserviceVersionMeta = microserviceVersionMeta;
    edgeInvocation.microserviceVersionRule = microserviceVersionRule;
    Deencapsulation.setField(edgeInvocation, "restOperationMeta", restOperationMeta);

    Object[] args = new Object[] {};
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getMicroservice();
        result = microservice;
      }
    };

    edgeInvocation.createInvocation(args);
    Invocation invocation = Deencapsulation.getField(edgeInvocation, "invocation");
    Assert.assertThat(invocation.getResponseExecutor(), Matchers.instanceOf(ReactiveResponseExecutor.class));
  }
}
