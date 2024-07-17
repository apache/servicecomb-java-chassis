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

package org.apache.servicecomb.samples;

import java.util.List;

import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.annotation.Transport;
import org.apache.servicecomb.localregistry.RegistryBean;
import org.apache.servicecomb.localregistry.RegistryBean.Instance;
import org.apache.servicecomb.localregistry.RegistryBean.Instances;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.vertx.core.http.WebSocket;

@Configuration
public class ThirdSvcConfiguration {
  @RequestMapping(path = "/ws")
  public interface WebsocketClient {
    @PostMapping("/websocket")
    @Transport(name = CoreConst.WEBSOCKET)
    WebSocket websocket();
  }

  @RequestMapping(path = "/")
  public interface ReactiveStreamClient {
    class Model {
      private String name;

      private int age;

      public Model() {

      }

      public Model(String name, int age) {
        this.name = name;
        this.age = age;
      }

      public int getAge() {
        return age;
      }

      public Model setAge(int age) {
        this.age = age;
        return this;
      }

      public String getName() {
        return name;
      }

      public Model setName(String name) {
        this.name = name;
        return this;
      }
    }

    @GetMapping("/sseString")
    Publisher<String> sseString();

    @GetMapping("/sseModel")
    Publisher<Model> sseModel();
  }

  @Bean
  public RegistryBean providerServiceBean() {
    return new RegistryBean()
        .addSchemaInterface("ReactiveStreamController", ReactiveStreamClient.class)
        .setAppId("demo-zookeeper")
        .setServiceName("provider")
        .setVersion("0.0.1")
        .setInstances(new Instances().setInstances(List.of(
            new Instance().setEndpoints(List.of("rest://localhost:9094")))));
  }

  @Bean
  public RegistryBean gatewayServiceBean() {
    return new RegistryBean()
        .addSchemaInterface("ReactiveStreamController", ReactiveStreamClient.class)
        .addSchemaInterface("WebsocketController", WebsocketClient.class)
        .setAppId("demo-zookeeper")
        .setServiceName("gateway")
        .setVersion("0.0.1")
        .setInstances(new Instances().setInstances(List.of(
            new Instance().setEndpoints(List.of("rest://localhost:9090?websocketEnabled=true")))));
  }

  @Bean("reactiveStreamProvider")
  public ReactiveStreamClient reactiveStreamProvider() {
    return Invoker.createProxy("provider", "ReactiveStreamController", ReactiveStreamClient.class);
  }

  @Bean("reactiveStreamGateway")
  public ReactiveStreamClient reactiveStreamGateway() {
    return Invoker.createProxy("gateway", "ReactiveStreamController", ReactiveStreamClient.class);
  }

  @Bean
  public WebsocketClient gatewayWebsocketClient() {
    return Invoker.createProxy("gateway", "WebsocketController", WebsocketClient.class);
  }
}
