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

package org.apache.servicecomb.serviceregistry.client.http;

import static org.hamcrest.core.Is.is;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceFactory;
import org.apache.servicecomb.serviceregistry.api.registry.ServiceCenterConfig;
import org.apache.servicecomb.serviceregistry.api.registry.ServiceCenterInfo;
import org.apache.servicecomb.serviceregistry.api.response.GetExistenceResponse;
import org.apache.servicecomb.serviceregistry.api.response.GetSchemaResponse;
import org.apache.servicecomb.serviceregistry.api.response.GetSchemasResponse;
import org.apache.servicecomb.serviceregistry.client.ClientException;
import org.apache.servicecomb.serviceregistry.client.IpPortManager;
import org.apache.servicecomb.serviceregistry.client.http.ServiceRegistryClientImpl.ResponseWrapper;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.Json;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestServiceRegistryClientImpl {
  @Mocked
  private IpPortManager ipPortManager;

  private ServiceRegistryClientImpl oClient = null;

  @Before
  public void setUp() throws Exception {
    oClient = new ServiceRegistryClientImpl(ipPortManager);

    new MockUp<RestUtils>() {
      @Mock
      void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
      }
    };

    new MockUp<CountDownLatch>() {
      @Mock
      public void await() {
      }
    };
  }

  @After
  public void tearDown() throws Exception {
    oClient = null;
  }

  @Test
  public void testPrivateMethodCreateHttpClientOptions() {
    MicroserviceFactory microserviceFactory = new MicroserviceFactory();
    Microservice microservice = microserviceFactory.create("app", "ms");
    oClient.registerMicroservice(microservice);
    oClient.registerMicroserviceInstance(microservice.getInstance());
    try {
      oClient.init();
      HttpClientOptions httpClientOptions = Deencapsulation.invoke(oClient, "createHttpClientOptions");
      Assert.assertNotNull(httpClientOptions);
      Assert.assertEquals(80, httpClientOptions.getDefaultPort());
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }

  @Test
  public void testException() {
    MicroserviceFactory microserviceFactory = new MicroserviceFactory();
    Microservice microservice = microserviceFactory.create("app", "ms");
    Assert.assertEquals(null, oClient.registerMicroservice(microservice));
    Assert.assertEquals(null, oClient.registerMicroserviceInstance(microservice.getInstance()));
    oClient.init();
    Assert.assertEquals(null,
        oClient.getMicroserviceId(microservice.getAppId(),
            microservice.getServiceName(),
            microservice.getVersion(),
            microservice.getEnvironment()));
    Assert.assertThat(oClient.getAllMicroservices().isEmpty(), is(true));
    Assert.assertEquals(null, oClient.registerMicroservice(microservice));
    Assert.assertEquals(null, oClient.getMicroservice("microserviceId"));
    Assert.assertEquals(null, oClient.getMicroserviceInstance("consumerId", "providerId"));
    Assert.assertEquals(false,
        oClient.unregisterMicroserviceInstance("microserviceId", "microserviceInstanceId"));
    Assert.assertEquals(null, oClient.heartbeat("microserviceId", "microserviceInstanceId"));
    Assert.assertEquals(null,
        oClient.findServiceInstance("selfMicroserviceId", "appId", "serviceName", "versionRule"));
    Assert.assertEquals(null,
        oClient.findServiceInstances("selfMicroserviceId", "appId", "serviceName", "versionRule", "0"));

    Assert.assertEquals("a", new ClientException("a").getMessage());
  }

  static abstract class RegisterSchemaTester {
    void run() {
      Logger rootLogger = Logger.getRootLogger();

      List<LoggingEvent> events = new ArrayList<>();
      Appender appender = new MockUp<Appender>() {
        @Mock
        public void doAppend(LoggingEvent event) {
          events.add(event);
        }
      }.getMockInstance();
      rootLogger.addAppender(appender);

      doRun(events);

      rootLogger.removeAppender(appender);
    }

    abstract void doRun(List<LoggingEvent> events);
  }

  @Test
  public void testRegisterSchemaNoResponse() {
    new RegisterSchemaTester() {
      void doRun(java.util.List<LoggingEvent> events) {
        oClient.registerSchema("msid", "schemaId", "content");
        Assert.assertEquals("Register schema msid/schemaId failed.", events.get(0).getMessage());
      }
    }.run();
  }

  @Test
  public void testRegisterSchemaException() {
    InterruptedException e = new InterruptedException();
    new MockUp<CountDownLatch>() {
      @Mock
      public void await() throws InterruptedException {
        throw e;
      }
    };

    new RegisterSchemaTester() {
      void doRun(java.util.List<LoggingEvent> events) {
        oClient.registerSchema("msid", "schemaId", "content");
        Assert.assertEquals(
            "register schema msid/schemaId fail.",
            events.get(0).getMessage());
        Assert.assertEquals(e, events.get(0).getThrowableInformation().getThrowable());
      }
    }.run();
  }

  @Test
  public void testRegisterSchemaErrorResponse() {
    new MockUp<ServiceRegistryClientImpl>() {
      @Mock
      Handler<RestResponse> syncHandlerEx(CountDownLatch countDownLatch, Holder<ResponseWrapper> holder) {
        return restResponse -> {
          HttpClientResponse response = Mockito.mock(HttpClientResponse.class);
          Mockito.when(response.statusCode()).thenReturn(400);
          Mockito.when(response.statusMessage()).thenReturn("client error");

          Buffer bodyBuffer = Buffer.buffer();
          bodyBuffer.appendString("too big");

          ResponseWrapper responseWrapper = new ResponseWrapper();
          responseWrapper.response = response;
          responseWrapper.bodyBuffer = bodyBuffer;
          holder.value = responseWrapper;
        };
      }
    };
    new MockUp<RestUtils>() {
      @Mock
      void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
        responseHandler.handle(null);
      }
    };

    new RegisterSchemaTester() {
      void doRun(java.util.List<LoggingEvent> events) {
        oClient.registerSchema("msid", "schemaId", "content");
        Assert.assertEquals(
            "Register schema msid/schemaId failed, statusCode: 400, statusMessage: client error, description: too big.",
            events.get(0).getMessage());
      }
    }.run();
  }

  @Test
  public void testRegisterSchemaSuccess() {
    new MockUp<ServiceRegistryClientImpl>() {
      @Mock
      Handler<RestResponse> syncHandlerEx(CountDownLatch countDownLatch, Holder<ResponseWrapper> holder) {
        return restResponse -> {
          HttpClientResponse response = Mockito.mock(HttpClientResponse.class);
          Mockito.when(response.statusCode()).thenReturn(200);

          ResponseWrapper responseWrapper = new ResponseWrapper();
          responseWrapper.response = response;
          holder.value = responseWrapper;
        };
      }
    };
    new MockUp<RestUtils>() {
      @Mock
      void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
        responseHandler.handle(null);
      }
    };

    new RegisterSchemaTester() {
      void doRun(java.util.List<LoggingEvent> events) {
        oClient.registerSchema("msid", "schemaId", "content");
        Assert.assertEquals(
            "register schema msid/schemaId success.",
            events.get(0).getMessage());
      }
    }.run();
  }

  @Test
  public void syncHandler_failed(@Mocked RequestContext requestContext,
      @Mocked HttpClientResponse response) {
    CountDownLatch countDownLatch = new CountDownLatch(1);
    Class<GetExistenceResponse> cls = GetExistenceResponse.class;
    Holder<GetExistenceResponse> holder = new Holder<>();
    Handler<RestResponse> handler = oClient.syncHandler(countDownLatch, cls, holder);

    Holder<Handler<Buffer>> bodyHandlerHolder = new Holder<>();
    new MockUp<HttpClientResponse>(response) {
      @Mock
      HttpClientResponse bodyHandler(Handler<Buffer> bodyHandler) {
        bodyHandlerHolder.value = bodyHandler;
        return null;
      }
    };
    new Expectations() {
      {
        response.statusCode();
        result = 400;
        response.statusMessage();
        result = Status.BAD_REQUEST.getReasonPhrase();
      }
    };

    RestResponse event = new RestResponse(requestContext, response);
    handler.handle(event);

    Buffer bodyBuffer = Buffer.buffer("{}");
    bodyHandlerHolder.value.handle(bodyBuffer);

    Assert.assertNull(holder.value);
  }

  @Test
  public void isSchemaExist() {
    String microserviceId = "msId";
    String schemaId = "schemaId";

    new MockUp<RestUtils>() {
      @Mock
      void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
        Holder<GetExistenceResponse> holder = Deencapsulation.getField(responseHandler, "arg$4");
        holder.value = new GetExistenceResponse();
      }
    };
    Assert.assertFalse(oClient.isSchemaExist(microserviceId, schemaId));
  }

  @Test
  public void getSchemas() {
    String microserviceId = "msId";

    new MockUp<RestUtils>() {
      @Mock
      void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
        Holder<GetSchemasResponse> holder = Deencapsulation.getField(responseHandler, "arg$4");
        GetSchemasResponse schemasResp = Json.decodeValue(
            "{\"schema\":[{\"schemaId\":\"metricsEndpoint\",\"summary\":\"c1188d709631a9038874f9efc6eb894f\"},{\"schemaId\":\"comment\",\"summary\":\"bfa81d625cfbd3a57f38745323e16824\"},"
                + "{\"schemaId\":\"healthEndpoint\",\"summary\":\"96a0aaaaa454cfa0c716e70c0017fe27\"}]}",
            GetSchemasResponse.class);
        holder.statusCode = 200;
        holder.value = schemasResp;
      }
    };
    Holder<List<GetSchemaResponse>> schemasHolder = oClient.getSchemas(microserviceId);
    List<GetSchemaResponse> schemaResponses = schemasHolder.getValue();
    Assert.assertEquals(200, schemasHolder.getStatusCode());
    Assert.assertEquals(3, schemaResponses.size());
    Assert.assertEquals("bfa81d625cfbd3a57f38745323e16824", schemaResponses.get(1).getSummary());
  }

  @Test
  public void getSchemasForNew() {
    String microserviceId = "msId";

    new MockUp<RestUtils>() {
      @Mock
      void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
        Holder<GetSchemasResponse> holder = Deencapsulation.getField(responseHandler, "arg$4");
        GetSchemasResponse schemasResp = Json.decodeValue(
            "{\"schemas\":[{\"schemaId\":\"metricsEndpoint\",\"summary\":\"c1188d709631a9038874f9efc6eb894f\"},{\"schemaId\":\"comment\",\"summary\":\"bfa81d625cfbd3a57f38745323e16824\"},"
                + "{\"schemaId\":\"healthEndpoint\",\"summary\":\"96a0aaaaa454cfa0c716e70c0017fe27\"}]}",
            GetSchemasResponse.class);
        holder.statusCode = 200;
        holder.value = schemasResp;
      }
    };
    Holder<List<GetSchemaResponse>> schemasHolder = oClient.getSchemas(microserviceId);
    List<GetSchemaResponse> schemas = schemasHolder.getValue();
    Assert.assertEquals(200, schemasHolder.getStatusCode());
    Assert.assertEquals(3, schemas.size());
    Assert.assertEquals("bfa81d625cfbd3a57f38745323e16824", schemas.get(1).getSummary());
  }

  @Test
  public void getSchemasFailed() {
    String microserviceId = "msId";

    new MockUp<RestUtils>() {
      @Mock
      void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
        Holder<GetSchemasResponse> holder = Deencapsulation.getField(responseHandler, "arg$4");
        holder.setStatusCode(Status.NOT_FOUND.getStatusCode());
      }
    };
    Holder<List<GetSchemaResponse>> schemasHolder = oClient.getSchemas(microserviceId);
    List<GetSchemaResponse> schemaResponses = schemasHolder.getValue();
    Assert.assertEquals(404, schemasHolder.getStatusCode());
    Assert.assertNull(schemaResponses);
  }

  @Test
  public void testFindServiceInstance() {
    Assert.assertNull(oClient.findServiceInstance("aaa", "bbb"));
  }

  @Test
  public void findServiceInstance_consumerId_null() {
    new MockUp<IpPortManager>(ipPortManager) {
      @Mock
      IpPort getAvailableAddress() {
        throw new Error("must not invoke this.");
      }
    };

    Assert.assertNull(oClient.findServiceInstance(null, "appId", "serviceName", "1.0.0+"));
  }

  @Test
  public void findServiceInstances_microserviceNotExist(@Mocked RequestContext requestContext) {
    HttpClientResponse response = new MockUp<HttpClientResponse>() {
      @Mock
      int statusCode() {
        return 400;
      }

      @Mock
      HttpClientResponse bodyHandler(Handler<Buffer> bodyHandler) {
        Buffer bodyBuffer = Buffer.buffer("{\"errorCode\":\"400012\"}");
        bodyHandler.handle(bodyBuffer);
        return null;
      }
    }.getMockInstance();
    RestResponse restResponse = new RestResponse(requestContext, response);
    new MockUp<RestUtils>() {
      @Mock
      void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
        responseHandler.handle(restResponse);
      }
    };
    MicroserviceInstances microserviceInstances = oClient
        .findServiceInstances("consumerId", "appId", "serviceName", DefinitionConst.VERSION_RULE_ALL, null);

    Assert.assertTrue(microserviceInstances.isMicroserviceNotExist());
    Assert.assertFalse(microserviceInstances.isNeedRefresh());
  }

  @Test
  public void testGetServiceCenterInfoSuccess() {
    ServiceCenterInfo serviceCenterInfo = new ServiceCenterInfo();
    serviceCenterInfo.setVersion("x.x.x");
    serviceCenterInfo.setBuildTag("xxx");
    serviceCenterInfo.setRunMode("dev");
    serviceCenterInfo.setApiVersion("x.x.x");
    serviceCenterInfo.setConfig(new ServiceCenterConfig());

    new MockUp<RestUtils>() {
      @Mock
      void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
        Holder<ServiceCenterInfo> holder = Deencapsulation.getField(responseHandler, "arg$4");
        holder.value = serviceCenterInfo;
      }
    };
    ServiceCenterInfo info = oClient.getServiceCenterInfo();
    Assert.assertEquals("x.x.x", info.getVersion());
    Assert.assertEquals("xxx", info.getBuildTag());
    Assert.assertEquals("dev", info.getRunMode());
    Assert.assertNotNull(info.getConfig());
  }

  @Test
  public void testGetServiceCenterInfoException() {
    InterruptedException e = new InterruptedException();
    new MockUp<CountDownLatch>() {
      @Mock
      public void await() throws InterruptedException {
        throw e;
      }
    };

    new RegisterSchemaTester() {
      void doRun(java.util.List<LoggingEvent> events) {
        oClient.getServiceCenterInfo();
        Assert.assertEquals(
            "query servicecenter version info failed.",
            events.get(0).getMessage());
        Assert.assertEquals(e, events.get(0).getThrowableInformation().getThrowable());
      }
    }.run();
  }
}
