/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.transport.rest.vertx.edge;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.codec.RestCodec;
import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.common.rest.filter.HttpServerFilter;
import io.servicecomb.common.rest.locator.OperationLocator;
import io.servicecomb.common.rest.locator.ServicePathManager;
import io.servicecomb.core.Handler;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.provider.consumer.ReactiveResponseExecutor;
import io.servicecomb.core.provider.consumer.ReferenceConfig;
import io.servicecomb.core.provider.consumer.ReferenceConfigUtils;
import io.servicecomb.foundation.common.exceptions.ServiceCombException;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
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
  public void prepareEdgeInvokeNoPath() throws Throwable {
    new Expectations(ReferenceConfigUtils.class) {
      {
        ReferenceConfigUtils.getForInvoke(microserviceName);
        result = referenceConfig;
      }
    };
    new Expectations(ServicePathManager.class) {
      {
        ServicePathManager.getServicePathManager(microserviceMeta);
        result = null;
      }
    };

    expectedException.expect(ServiceCombException.class);
    expectedException.expectMessage("no schema defined for app:app:ms");

    edgeInvocation.prepareEdgeInvoke();
  }

  @Test
  public void prepareEdgeInvokeNormal(@Mocked ServicePathManager servicePathManager, @Mocked OperationLocator locator,
      @Mocked RestOperationMeta restOperationMeta) throws Throwable {
    new Expectations(ReferenceConfigUtils.class) {
      {
        ReferenceConfigUtils.getForInvoke(microserviceName);
        result = referenceConfig;
      }
    };

    Microservice microservice = new Microservice();
    microservice.setServiceName(microserviceName);
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getMicroservice();
        result = microservice;
      }
    };

    Object args[] = new Object[0];
    new Expectations(RestCodec.class) {
      {
        RestCodec.restToArgs(requestEx, restOperationMeta);
        result = args;
      }
    };

    Map<String, String> pathParams = new HashMap<>();
    new Expectations(ServicePathManager.class) {
      {
        ServicePathManager.getServicePathManager(microserviceMeta);
        result = servicePathManager;
        servicePathManager.consumerLocateOperation(requestEx.getRequestURI(), requestEx.getMethod());
        result = locator;
        locator.getOperation();
        result = restOperationMeta;
        locator.getPathVarMap();
        result = pathParams;
      }
    };

    edgeInvocation.prepareEdgeInvoke();

    Assert.assertSame(pathParams, requestEx.getAttribute(RestConst.PATH_PARAMETERS));
    Invocation invocation = Deencapsulation.getField(edgeInvocation, "invocation");
    Assert.assertSame(args, invocation.getSwaggerArguments());
  }

  @Test
  public void prepareInvoke(@Mocked OperationMeta operationMeta) throws Throwable {
    AtomicInteger count = new AtomicInteger();
    edgeInvocation = new EdgeInvocation() {
      @Override
      protected void prepareEdgeInvoke() throws Throwable {
        count.incrementAndGet();
      }

      @Override
      protected void initProduceProcessor() {
      }

      @Override
      protected void setContext() throws Exception {
      }
    };

    Invocation invocation = new Invocation(referenceConfig, operationMeta, null);
    Deencapsulation.setField(edgeInvocation, "invocation", invocation);

    edgeInvocation.prepareInvoke();

    Assert.assertEquals(1, count.get());
  }

  @Test
  public void doInvoke(@Mocked SchemaMeta schemaMeta, @Mocked OperationMeta operationMeta, @Mocked Handler handler)
      throws Throwable {
    new Expectations() {
      {
        operationMeta.getSchemaMeta();
        result = schemaMeta;
        schemaMeta.getConsumerHandlerChain();
        result = Arrays.asList(handler);
      }
    };

    Invocation invocation = new Invocation(referenceConfig, operationMeta, null);
    Deencapsulation.setField(edgeInvocation, "invocation", invocation);

    edgeInvocation.doInvoke();

    Assert.assertThat(invocation.getResponseExecutor(), Matchers.instanceOf(ReactiveResponseExecutor.class));
  }
}
