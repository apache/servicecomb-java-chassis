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

package io.servicecomb.edge.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.Holder;

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
import io.servicecomb.core.definition.MicroserviceVersionMeta;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.provider.consumer.ReactiveResponseExecutor;
import io.servicecomb.core.provider.consumer.ReferenceConfig;
import io.servicecomb.foundation.common.exceptions.ServiceCombException;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.consumer.AppManager;
import io.servicecomb.serviceregistry.consumer.MicroserviceVersionRule;
import io.servicecomb.serviceregistry.definition.DefinitionConst;
import io.servicecomb.swagger.invocation.Response;
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
  public void prepareEdgeInvokeNoPath(@Mocked MicroserviceVersionMeta microserviceVersionMeta) throws Throwable {
    edgeInvocation.latestMicroserviceVersionMeta = microserviceVersionMeta;
    edgeInvocation.microserviceVersionRule = new MicroserviceVersionRule("app", "ms", "1.0.0");

    new Expectations(ServicePathManager.class) {
      {
        microserviceVersionMeta.getMicroserviceMeta();
        result = microserviceMeta;
        ServicePathManager.getServicePathManager(microserviceMeta);
        result = null;
      }
    };

    expectedException.expect(ServiceCombException.class);
    expectedException.expectMessage("no schema defined for app:app:ms");

    edgeInvocation.prepareEdgeInvoke();
  }

  @Test
  public void prepareEdgeInvokeNormal(@Mocked MicroserviceVersionMeta microserviceVersionMeta,
      @Mocked ServicePathManager servicePathManager, @Mocked OperationLocator locator,
      @Mocked RestOperationMeta restOperationMeta) throws Throwable {
    edgeInvocation.latestMicroserviceVersionMeta = microserviceVersionMeta;
    edgeInvocation.microserviceVersionRule = new MicroserviceVersionRule("app", "ms", "1.0.0");

    Microservice microservice = new Microservice();
    microservice.setServiceName(microserviceName);
    new Expectations(RegistryUtils.class) {
      {
        microserviceVersionMeta.getMicroserviceMeta();
        result = microserviceMeta;
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
  public void chooseVersionRule() {
    Assert.assertEquals(DefinitionConst.VERSION_RULE_ALL, edgeInvocation.chooseVersionRule());
  }

  @Test
  public void findMicroserviceVersionMetaNullLatestVersion(@Mocked AppManager appManager,
      @Mocked MicroserviceVersionRule microserviceVersionRule) {
    String versionRule = DefinitionConst.VERSION_RULE_ALL;
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getAppId();
        result = "app";
        appManager.getOrCreateMicroserviceVersionRule("app", microserviceName, versionRule);
        result = microserviceVersionRule;
        microserviceVersionRule.getLatestMicroserviceVersion();
        result = null;
      }
    };
    Deencapsulation.setField(edgeInvocation, "appManager", appManager);

    expectedException.expect(ServiceCombException.class);
    expectedException.expectMessage(Matchers
        .is("Failed to find latest MicroserviceVersionMeta, appId=app, microserviceName=ms, versionRule=0+."));
    edgeInvocation.findMicroserviceVersionMeta();
  }

  @Test
  public void findMicroserviceVersionMetaNormal(@Mocked AppManager appManager,
      @Mocked MicroserviceVersionRule microserviceVersionRule,
      @Mocked MicroserviceVersionMeta latestMicroserviceVersionMeta) {
    String versionRule = DefinitionConst.VERSION_RULE_ALL;
    microserviceName = "app:ms";
    edgeInvocation.microserviceName = microserviceName;
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getAppId();
        result = "app";
        appManager.getOrCreateMicroserviceVersionRule("app", microserviceName, versionRule);
        result = microserviceVersionRule;
        microserviceVersionRule.getLatestMicroserviceVersion();
        result = latestMicroserviceVersionMeta;
      }
    };
    Deencapsulation.setField(edgeInvocation, "appManager", appManager);

    edgeInvocation.findMicroserviceVersionMeta();

    Assert.assertSame(latestMicroserviceVersionMeta, edgeInvocation.latestMicroserviceVersionMeta);
  }

  class EdgeInvocationForTestClassLoader extends EdgeInvocation {
    ClassLoader findMicroserviceVersionMetaClassLoader;

    ClassLoader prepareEdgeInvokeClassLoader;

    ClassLoader prepareInvokeClassLoader;

    ClassLoader doInvokeClassLoader;

    Throwable doInvokeException;

    @Override
    protected void findMicroserviceVersionMeta() {
      findMicroserviceVersionMetaClassLoader = Thread.currentThread().getContextClassLoader();
    }

    @Override
    protected void prepareEdgeInvoke() throws Throwable {
      prepareEdgeInvokeClassLoader = Thread.currentThread().getContextClassLoader();
    }

    @Override
    protected Response prepareInvoke() throws Throwable {
      prepareInvokeClassLoader = Thread.currentThread().getContextClassLoader();
      return null;
    }

    @Override
    protected void doInvoke() throws Throwable {
      if (doInvokeException != null) {
        throw doInvokeException;
      }

      doInvokeClassLoader = Thread.currentThread().getContextClassLoader();
    }
  }

  @Test
  public void invokeNormal(@Mocked MicroserviceVersionMeta latestMicroserviceVersionMeta,
      @Mocked MicroserviceMeta microserviceMeta) {
    ClassLoader org = Thread.currentThread().getContextClassLoader();
    ClassLoader microserviceClassLoader = new ClassLoader() {
    };

    new Expectations() {
      {
        latestMicroserviceVersionMeta.getMicroserviceMeta();
        result = microserviceMeta;
        microserviceMeta.getClassLoader();
        result = microserviceClassLoader;
      }
    };
    EdgeInvocationForTestClassLoader invocation = new EdgeInvocationForTestClassLoader();
    invocation.latestMicroserviceVersionMeta = latestMicroserviceVersionMeta;

    invocation.invoke();

    Assert.assertSame(org, invocation.findMicroserviceVersionMetaClassLoader);
    Assert.assertSame(microserviceClassLoader, invocation.prepareEdgeInvokeClassLoader);
    Assert.assertSame(microserviceClassLoader, invocation.prepareInvokeClassLoader);
    Assert.assertSame(microserviceClassLoader, invocation.doInvokeClassLoader);
    Assert.assertSame(org, Thread.currentThread().getContextClassLoader());
  }

  @Test
  public void invokePrepareHaveResponse(@Mocked MicroserviceVersionMeta latestMicroserviceVersionMeta,
      @Mocked MicroserviceMeta microserviceMeta, @Mocked Response response) {
    ClassLoader microserviceClassLoader = new ClassLoader() {
    };

    new Expectations() {
      {
        latestMicroserviceVersionMeta.getMicroserviceMeta();
        result = microserviceMeta;
        microserviceMeta.getClassLoader();
        result = microserviceClassLoader;
      }
    };

    Holder<Response> result = new Holder<>();
    EdgeInvocationForTestClassLoader invocation = new EdgeInvocationForTestClassLoader() {
      @Override
      protected Response prepareInvoke() throws Throwable {
        return response;
      }

      @Override
      protected void sendResponse(Response response) throws Exception {
        result.value = response;
      }

      @Override
      protected void doInvoke() throws Throwable {
        result.value = Response.ok("do not run to here");
      }
    };
    invocation.latestMicroserviceVersionMeta = latestMicroserviceVersionMeta;

    invocation.invoke();

    Assert.assertSame(response, result.value);
  }

  @Test
  public void invokeException(@Mocked MicroserviceVersionMeta latestMicroserviceVersionMeta,
      @Mocked MicroserviceMeta microserviceMeta) {
    ClassLoader org = Thread.currentThread().getContextClassLoader();
    ClassLoader microserviceClassLoader = new ClassLoader() {
    };

    new Expectations() {
      {
        latestMicroserviceVersionMeta.getMicroserviceMeta();
        result = microserviceMeta;
        microserviceMeta.getClassLoader();
        result = microserviceClassLoader;
      }
    };

    Throwable doInvokeException = new Error();

    EdgeInvocationForTestClassLoader invocation = new EdgeInvocationForTestClassLoader();
    invocation.latestMicroserviceVersionMeta = latestMicroserviceVersionMeta;
    invocation.doInvokeException = doInvokeException;

    expectedException.expect(ServiceCombException.class);
    expectedException.expectMessage(Matchers.is("unknown edge exception."));
    expectedException.expectCause(Matchers.sameInstance(doInvokeException));

    invocation.invoke();

    Assert.assertSame(org, invocation.findMicroserviceVersionMetaClassLoader);
    Assert.assertSame(microserviceClassLoader, invocation.prepareEdgeInvokeClassLoader);
    Assert.assertSame(microserviceClassLoader, invocation.prepareInvokeClassLoader);
    Assert.assertSame(microserviceClassLoader, invocation.doInvokeClassLoader);
    Assert.assertSame(org, Thread.currentThread().getContextClassLoader());
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
