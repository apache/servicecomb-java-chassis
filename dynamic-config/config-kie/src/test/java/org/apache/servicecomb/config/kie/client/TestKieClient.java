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

package org.apache.servicecomb.config.kie.client;

import com.google.common.eventbus.Subscribe;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.kie.archaius.sources.KieConfigurationSourceImpl;
import org.apache.servicecomb.config.kie.archaius.sources.KieConfigurationSourceImpl.UpdateHandler;
import org.apache.servicecomb.config.kie.client.KieClient.ConfigRefresh;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.vertx.client.ClientPoolManager;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientWithContext.RunHandler;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

@SuppressWarnings("deprecation")
public class TestKieClient {

  String mockKvResponse = "{\n"
      + "  \"data\": [\n"
      + "    {\n"
      + "      \"id\": \"string\",\n"
      + "      \"check\": \"string\",\n"
      + "      \"domain\": \"string\",\n"
      + "      \"key\": \"string\",\n"
      + "      \"label_id\": \"string\",\n"
      + "      \"labels\": {\n"
      + "        \"additionalProp1\": \"string\",\n"
      + "        \"additionalProp2\": \"string\",\n"
      + "        \"additionalProp3\": \"string\"\n"
      + "      },\n"
      + "      \"project\": \"string\",\n"
      + "      \"revision\": 0,\n"
      + "      \"value\": \"string\",\n"
      + "      \"value_type\": \"string\"\n"
      + "    }\n"
      + "  ],\n"
      + "  \"label\": {\n"
      + "    \"label_id\": \"string\",\n"
      + "    \"labels\": {\n"
      + "      \"additionalProp1\": \"string\",\n"
      + "      \"additionalProp2\": \"string\",\n"
      + "      \"additionalProp3\": \"string\"\n"
      + "    }\n"
      + "  },\n"
      + "  \"num\": 0,\n"
      + "  \"size\": 0,\n"
      + "  \"total\": 0\n"
      + "}";

  @BeforeClass
  public static void setUpClass() {
    KieConfig.setFinalConfig(ConfigUtil.createLocalConfig());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testRefreshKieConfig() {
    HttpClientRequest request = Mockito.mock(HttpClientRequest.class);
    Mockito.when(request.method()).thenReturn(HttpMethod.GET);
    Mockito.when(request.headers()).thenReturn(MultiMap.caseInsensitiveMultiMap());
    Buffer rsp = Mockito.mock(Buffer.class);
    Mockito.when(rsp.toJsonObject()).thenReturn(new JsonObject(mockKvResponse));
    HttpClientResponse event = Mockito.mock(HttpClientResponse.class);
    Mockito.when(event.bodyHandler(Mockito.any(Handler.class))).then(invocation -> {
      Handler<Buffer> handler = invocation.getArgumentAt(0, Handler.class);
      handler.handle(rsp);
      return null;
    });
    Mockito.when(event.statusCode()).thenReturn(200);
    HttpClient httpClient = Mockito.mock(HttpClient.class);
    Mockito.when(
        httpClient.get(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(),
            Mockito.any(Handler.class)))
        .then(invocation -> {
          Handler<HttpClientResponse> handler = invocation.getArgumentAt(3, Handler.class);
          handler.handle(event);
          return request;
        });
    new MockUp<HttpClientWithContext>() {
      @Mock
      public void runOnContext(RunHandler handler) {
        handler.run(httpClient);
      }
    };
    UpdateHandler updateHandler = new KieConfigurationSourceImpl().new UpdateHandler();
    KieClient kie = new KieClient(updateHandler);
    kie.refreshKieConfig();
  }


  public static class ConfigRefreshExceptionEvent {
    Map<String, String> map;

    public ConfigRefreshExceptionEvent(Map<String, String> map) {
      this.map = map;
    }

    @Subscribe
    public void testMsg(Object event) {
      if (event instanceof ConnFailEvent) {
        map.put("result", "Fail event trigger");
      }
      if (event instanceof ConnSuccEvent) {
        map.put("result", "Succ event trigger");
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testConfigRefreshException(@Mocked ClientPoolManager<HttpClientWithContext> clientMgr,
      @Mocked HttpClientWithContext httpClientWithContext) {
    KieConfigurationSourceImpl impl = new KieConfigurationSourceImpl();
    Map<String, String> map = new HashMap<>();
    EventManager.register(new ConfigRefreshExceptionEvent(map));
    UpdateHandler updateHandler = impl.new UpdateHandler();
    HttpClientRequest request = Mockito.mock(HttpClientRequest.class);
    Mockito.when(request.headers()).thenReturn(MultiMap.caseInsensitiveMultiMap());
    Buffer rsp = Mockito.mock(Buffer.class);
    Mockito.when(rsp.toString()).thenReturn(mockKvResponse);

    HttpClientResponse event = Mockito.mock(HttpClientResponse.class);
    Mockito.when(event.bodyHandler(Mockito.any(Handler.class))).then(invocation -> {
      Handler<Buffer> handler = invocation.getArgumentAt(0, Handler.class);
      handler.handle(rsp);
      return null;
    });
    Mockito.when(event.statusCode()).thenReturn(400);
    Buffer buf = Mockito.mock(Buffer.class);
    Mockito.when(buf.toJsonObject()).thenReturn(new JsonObject(mockKvResponse));
    HttpClient httpClient = Mockito.mock(HttpClient.class);
    Mockito.when(
        httpClient.get(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(),
            Mockito.any(Handler.class)))
        .then(invocation -> {
          Handler<HttpClientResponse> handler = invocation.getArgumentAt(3, Handler.class);
          handler.handle(event);
          return request;
        });
    new MockUp<HttpClientWithContext>() {
      @Mock
      public void runOnContext(RunHandler handler) {
        handler.run(httpClient);
      }
    };
    new Expectations() {
      {
        clientMgr.findThreadBindClientPool();
        result = httpClientWithContext;
      }
    };
    KieClient kie = new KieClient(updateHandler);
    Deencapsulation.setField(kie, "clientMgr", clientMgr);
    ConfigRefresh refresh = kie.new ConfigRefresh("http://configcentertest:30103");
    refresh.run();
    Assert.assertEquals("Fail event trigger", map.get("result"));
    Mockito.when(event.statusCode()).thenReturn(200);
    refresh.run();
    Assert.assertEquals("Succ event trigger", map.get("result"));
  }

  @Test
  public void destroy() {
    KieClient kieClient = new KieClient(null);
    ScheduledExecutorService executor = Deencapsulation.getField(kieClient, "EXECUTOR");
    Assert.assertFalse(executor.isShutdown());
    executor.shutdown();
    Assert.assertTrue(executor.isShutdown());
  }
}
