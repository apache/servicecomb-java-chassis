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

package org.apache.servicecomb.demo.localRegistryClient;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.localregistry.RegistryBean;
import org.apache.servicecomb.localregistry.RegistryBean.Instance;
import org.apache.servicecomb.localregistry.RegistryBean.Instances;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegistryBeansConfiguration {
  @Bean
  public RegistryBean demoLocalRegistryServerBean() {
    List<String> endpoints = new ArrayList<>();
    endpoints.add("rest://localhost:8080");
    List<Instance> instances = new ArrayList<>();
    instances.add(new Instance().setEndpoints(endpoints));

    return new RegistryBean()
        .setServiceName("demo-local-registry-server-bean")
        .setId("002")
        .setVersion("0.0.3")
        .setAppId("demo-local-registry")
        .addSchemaId("ServerEndpoint")
        .addSchemaId("CodeFirstEndpoint")
        .setInstances(new Instances().setInstances(instances));
  }

  @Bean
  public RegistryBean demoLocalRegistryServerBean2() {
    List<String> endpoints = new ArrayList<>();
    endpoints.add("rest://localhost:8080");
    List<Instance> instances = new ArrayList<>();
    instances.add(new Instance().setEndpoints(endpoints));

    return new RegistryBean()
        .setServiceName("demo-local-registry-server-bean2")
        .setId("003")
        .setVersion("0.0.3")
        .setAppId("demo-local-registry")
        .addSchemaInterface("CodeFirstEndpoint2", CodeFirstService.class)
        .setInstances(new Instances().setInstances(instances));
  }
}
