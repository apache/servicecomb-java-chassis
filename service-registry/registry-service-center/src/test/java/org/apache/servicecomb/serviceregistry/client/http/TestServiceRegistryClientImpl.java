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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClients;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceFactory;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.ServiceCenterConfig;
import org.apache.servicecomb.serviceregistry.api.registry.ServiceCenterInfo;
import org.apache.servicecomb.serviceregistry.api.response.GetExistenceResponse;
import org.apache.servicecomb.serviceregistry.api.response.GetSchemaResponse;
import org.apache.servicecomb.serviceregistry.api.response.GetSchemasResponse;
import org.apache.servicecomb.serviceregistry.api.response.GetServiceResponse;
import org.apache.servicecomb.serviceregistry.client.ClientException;
import org.apache.servicecomb.serviceregistry.client.http.ServiceRegistryClientImpl.ResponseWrapper;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.Json;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestServiceRegistryClientImpl {
  private ServiceRegistryClientImpl oClient = null;

  private final Microservice microservice = new Microservice();

  @Before
  public void setUp() throws Exception {
    HttpClients.load();

    oClient = new ServiceRegistryClientImpl(ServiceRegistryConfig.buildFromConfiguration());

    new MockUp<RegistryUtils>() {
      @Mock
      Microservice getMicroservice() {
        return microservice;
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
    HttpClients.destroy();
  }

  @Test
  public void testException() {
    ArchaiusUtils.setProperty(BootStrapProperties.CONFIG_SERVICE_APPLICATION, "app");
    ArchaiusUtils.setProperty(BootStrapProperties.CONFIG_SERVICE_NAME, "ms");
    MicroserviceFactory microserviceFactory = new MicroserviceFactory();
    Microservice microservice = microserviceFactory.create();
    Assertions.assertNull(oClient.registerMicroservice(microservice));
    Assertions.assertNull(oClient.registerMicroserviceInstance(microservice.getInstance()));
    oClient.init();
    Assertions.assertNull(oClient.getMicroserviceId(microservice.getAppId(),
        microservice.getServiceName(),
        microservice.getVersion(),
        microservice.getEnvironment()));
    MatcherAssert.assertThat(oClient.getAllMicroservices().isEmpty(), is(true));
    Assertions.assertNull(oClient.registerMicroservice(microservice));
    Assertions.assertNull(oClient.getMicroservice("microserviceId"));
    Assertions.assertNull(oClient.getMicroserviceInstance("consumerId", "providerId"));
    Assertions.assertFalse(oClient.unregisterMicroserviceInstance("microserviceId", "microserviceInstanceId"));
    Assertions.assertNull(oClient.heartbeat("microserviceId", "microserviceInstanceId"));
    Assertions.assertNull(oClient.findServiceInstance("selfMicroserviceId", "appId", "serviceName", "versionRule"));
    Assertions.assertNull(
        oClient.findServiceInstances("selfMicroserviceId", "appId", "serviceName", "versionRule", "0"));

    Assertions.assertEquals("a", new ClientException("a").getMessage());

    ArchaiusUtils.resetConfig();
  }

  abstract static class RegisterSchemaTester {
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

      try {
        doRun(events);
      } catch (Throwable e) {
        e.printStackTrace();
      }

      rootLogger.removeAppender(appender);
    }

    abstract void doRun(List<LoggingEvent> events);
  }

  @Test
  public void testRegisterSchemaNoResponse() {
    new MockUp<RestClientUtil>() {
      @Mock
      void put(IpPort ipPort, String uri, RequestParam requestParam,
          Handler<RestResponse> responseHandler) {
        // do nothing to mock null response
      }
    };

    new RegisterSchemaTester() {
      void doRun(java.util.List<LoggingEvent> events) {
        oClient.registerSchema("msid", "schemaId", "content");
        Assertions.assertEquals("Register schema msid/schemaId failed.", events.get(0).getMessage());
      }
    }.run();
  }

  @Test
  public void testRegisterSchemaException() {
    new MockUp<RestClientUtil>() {
      @Mock
      void put(IpPort ipPort, String uri, RequestParam requestParam,
          Handler<RestResponse> responseHandler) {
        // do nothing to mock null response
      }
    };

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
        Assertions.assertEquals(
            "register schema msid/schemaId fail.",
            events.get(0).getMessage());
        Assertions.assertEquals(e, events.get(0).getThrowableInformation().getThrowable());
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
    new MockUp<RestClientUtil>() {
      @Mock
      void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
        responseHandler.handle(null);
      }
    };

    new RegisterSchemaTester() {
      void doRun(java.util.List<LoggingEvent> events) {
        oClient.registerSchema("msid", "schemaId", "content");
        Assertions.assertEquals(
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
    new MockUp<RestClientUtil>() {
      @Mock
      void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
        responseHandler.handle(null);
      }
    };

    new RegisterSchemaTester() {
      void doRun(java.util.List<LoggingEvent> events) {
        oClient.registerSchema("msid", "schemaId", "content");
        Assertions.assertEquals(
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

    Assertions.assertNull(holder.value);
  }

  @Test
  public void isSchemaExist() {
    String microserviceId = "msId";
    String schemaId = "schemaId";

    new MockUp<RestClientUtil>() {
      @Mock
      void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
        Holder<GetExistenceResponse> holder = Deencapsulation.getField(responseHandler, "arg$4");
        holder.value = new GetExistenceResponse();
      }
    };
    Assertions.assertFalse(oClient.isSchemaExist(microserviceId, schemaId));
  }

  @Test
  public void getAggregatedMicroservice() {
    String microserviceId = "msId";
    new MockUp<RestClientUtil>() {
      @Mock
      void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
        Holder<GetServiceResponse> holder = Deencapsulation.getField(responseHandler, "arg$4");
        holder.value = Json
            .decodeValue(
                "{\"service\":{\"serviceId\":\"serviceId\",\"framework\":null"
                    + ",\"registerBy\":null,\"environment\":null,\"appId\":\"appId\",\"serviceName\":null,"
                    + "\"alias\":null,\"version\":null,\"description\":null,\"level\":null,\"schemas\":[],"
                    + "\"paths\":[],\"status\":\"UP\",\"properties\":{},\"intance\":null}}",
                GetServiceResponse.class);
        RequestParam requestParam = requestContext.getParams();
        Assertions.assertEquals("global=true", requestParam.getQueryParams());
      }
    };
    Microservice aggregatedMicroservice = oClient.getAggregatedMicroservice(microserviceId);
    Assertions.assertEquals("serviceId", aggregatedMicroservice.getServiceId());
    Assertions.assertEquals("appId", aggregatedMicroservice.getAppId());
  }

  @Test
  public void getAggregatedSchema() {
    String microserviceId = "msId";
    String schemaId = "schemaId";

    new MockUp<RestClientUtil>() {

      @Mock
      void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
        Holder<GetSchemaResponse> holder = Deencapsulation.getField(responseHandler, "arg$4");
        holder.value = Json
            .decodeValue(
                "{ \"schema\": \"schema\", \"schemaId\":\"metricsEndpoint\",\"summary\":\"c1188d709631a9038874f9efc6eb894f\"}",
                GetSchemaResponse.class);
        RequestParam requestParam = requestContext.getParams();
        Assertions.assertEquals("global=true", requestParam.getQueryParams());
      }
    };

    LoadingCache<String, Map<String, String>> oldCache = Deencapsulation.getField(oClient, "schemaCache");
    LoadingCache<String, Map<String, String>> newCache = CacheBuilder.newBuilder()
        .expireAfterAccess(60, TimeUnit.SECONDS).build(new CacheLoader<String, Map<String, String>>() {
          public Map<String, String> load(String key) {
            Map<String, String> schemas = new HashMap<>();
            return schemas;
          }
        });
    Deencapsulation.setField(oClient, "schemaCache", newCache);

    String str = oClient.getAggregatedSchema(microserviceId, schemaId);
    Assertions.assertEquals("schema", str);

    Deencapsulation.setField(oClient, "schemaCache", oldCache);
  }

  @Test
  public void getSchemas() {
    String microserviceId = "msId";

    new MockUp<RestClientUtil>() {
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
    Assertions.assertEquals(200, schemasHolder.getStatusCode());
    Assertions.assertEquals(3, schemaResponses.size());
    Assertions.assertEquals("bfa81d625cfbd3a57f38745323e16824", schemaResponses.get(1).getSummary());
  }

  @Test
  public void getSchemasForNew() {
    String microserviceId = "msId";

    new MockUp<RestClientUtil>() {
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
    Assertions.assertEquals(200, schemasHolder.getStatusCode());
    Assertions.assertEquals(3, schemas.size());
    Assertions.assertEquals("bfa81d625cfbd3a57f38745323e16824", schemas.get(1).getSummary());
  }

  @Test
  public void getSchemasFailed() {
    String microserviceId = "msId";

    new MockUp<RestClientUtil>() {
      @Mock
      void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
        Holder<GetSchemasResponse> holder = Deencapsulation.getField(responseHandler, "arg$4");
        holder.setStatusCode(Status.NOT_FOUND.getStatusCode());
      }
    };
    Holder<List<GetSchemaResponse>> schemasHolder = oClient.getSchemas(microserviceId);
    List<GetSchemaResponse> schemaResponses = schemasHolder.getValue();
    Assertions.assertEquals(404, schemasHolder.getStatusCode());
    Assertions.assertNull(schemaResponses);
  }

  @Test
  public void testFindServiceInstance() {
    new MockUp<RestClientUtil>() {
      @Mock
      void get(IpPort ipPort, String uri, RequestParam requestParam,
          Handler<RestResponse> responseHandler) {
        Assertions.assertEquals("global=true", requestParam.getQueryParams());
      }
    };
    Assertions.assertNull(oClient.findServiceInstance("aaa", "bbb"));
  }

  @Test
  public void findServiceInstance_consumerId_null() {
    new MockUp<RestClientUtil>() {
      @Mock
      void get(IpPort ipPort, String uri, RequestParam requestParam,
          Handler<RestResponse> responseHandler) {
        Assertions.assertEquals("appId=appId&global=true&serviceName=serviceName&version=1.0.0%2B",
            requestParam.getQueryParams());
      }
    };
    Assertions.assertNull(oClient.findServiceInstance(null, "appId", "serviceName", "1.0.0+"));
  }

  @Test
  public void findServiceInstances_microserviceNotExist() {
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
    RestResponse restResponse = new RestResponse(null, response);
    new MockUp<RestClientUtil>() {
      @Mock
      void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
        Assertions.assertEquals("appId=appId&global=true&serviceName=serviceName&version=0.0.0.0%2B",
            requestContext.getParams().getQueryParams());
        restResponse.setRequestContext(requestContext);
        responseHandler.handle(restResponse);
      }
    };
    MicroserviceInstances microserviceInstances = oClient
        .findServiceInstances("consumerId", "appId", "serviceName", DefinitionConst.VERSION_RULE_ALL, null);

    Assertions.assertTrue(microserviceInstances.isMicroserviceNotExist());
    Assertions.assertFalse(microserviceInstances.isNeedRefresh());
  }

  @Test
  public void testGetServiceCenterInfoSuccess() {
    ServiceCenterInfo serviceCenterInfo = new ServiceCenterInfo();
    serviceCenterInfo.setVersion("x.x.x");
    serviceCenterInfo.setBuildTag("xxx");
    serviceCenterInfo.setRunMode("dev");
    serviceCenterInfo.setApiVersion("x.x.x");
    serviceCenterInfo.setConfig(new ServiceCenterConfig());

    new MockUp<RestClientUtil>() {
      @Mock
      void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
        Holder<ServiceCenterInfo> holder = Deencapsulation.getField(responseHandler, "arg$4");
        holder.value = serviceCenterInfo;
      }
    };
    ServiceCenterInfo info = oClient.getServiceCenterInfo();
    Assertions.assertEquals("x.x.x", info.getVersion());
    Assertions.assertEquals("xxx", info.getBuildTag());
    Assertions.assertEquals("dev", info.getRunMode());
    Assertions.assertNotNull(info.getConfig());
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
        Assertions.assertEquals(
            "query servicecenter version info failed.",
            events.get(0).getMessage());
        Assertions.assertEquals(e, events.get(0).getThrowableInformation().getThrowable());
      }
    }.run();
  }

  @Test
  public void updateMicroserviceInstanceStatus() {
    HttpClientResponse httpClientResponse = new MockUp<HttpClientResponse>() {
      @Mock
      int statusCode() {
        return 200;
      }
    }.getMockInstance();
    new MockUp<RestClientUtil>() {
      @Mock
      void put(IpPort ipPort, String uri, RequestParam requestParam, Handler<RestResponse> responseHandler) {
        Holder<HttpClientResponse> holder = Deencapsulation.getField(responseHandler, "arg$4");
        holder.value = httpClientResponse;
      }
    };

    boolean result = oClient.updateMicroserviceInstanceStatus
        ("svcId", "instanceId", MicroserviceInstanceStatus.UP);
    Assertions.assertTrue(result);
  }

  @Test
  public void updateMicroserviceInstanceStatus_response_failure() {
    HttpClientResponse httpClientResponse = new MockUp<HttpClientResponse>() {
      @Mock
      int statusCode() {
        return 400;
      }
    }.getMockInstance();
    new MockUp<RestClientUtil>() {
      @Mock
      void put(IpPort ipPort, String uri, RequestParam requestParam, Handler<RestResponse> responseHandler) {
        Holder<HttpClientResponse> holder = Deencapsulation.getField(responseHandler, "arg$4");
        holder.value = httpClientResponse;
      }
    };

    boolean result = oClient.updateMicroserviceInstanceStatus
        ("svcId", "instanceId", MicroserviceInstanceStatus.UP);
    Assertions.assertFalse(result);
  }

  private void shouldThrowException() {
    Assertions.fail("an exception is expected");
  }
}
