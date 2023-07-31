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

package org.apache.servicecomb.demo.springmvc.third;

import java.util.List;

import org.apache.servicecomb.localregistry.RegistryBean;
import org.apache.servicecomb.localregistry.RegistryBean.Instance;
import org.apache.servicecomb.localregistry.RegistryBean.Instances;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.config.DynamicPropertyFactory;

/**
 * see: https://github.com/apache/servicecomb-java-chassis/issues/2534
 */
@Configuration
public class Register {
  @Bean
  public RegistryBean thirdRegistryBean() {
    String endpoint;
    if (DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.test.vert.transport", true).get()) {
      endpoint = "rest://localhost:8080?sslEnabled=false&urlPrefix=%2Fapi";
    } else {
      endpoint = "rest://localhost:8080?sslEnabled=false";
    }

    return new RegistryBean().addSchemaInterface("heartbeat", HealthSchema.class)
        .setAppId("springmvctest")
        .setServiceName("third")
        .setVersion("0.0.1")
        .setInstances(new Instances().setInstances(List.of(new Instance().setEndpoints(List.of(endpoint)))));
  }
}
