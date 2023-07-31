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
package org.apache.servicecomb.demo.springmvc.client;

import java.util.Date;
import java.util.List;

import org.apache.servicecomb.localregistry.RegistryBean;
import org.apache.servicecomb.localregistry.RegistryBean.Instance;
import org.apache.servicecomb.localregistry.RegistryBean.Instances;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.netflix.config.DynamicPropertyFactory;

import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Configuration
public class ThirdSvc {
  @RequestMapping(path = "/codeFirstSpringmvc")
  public interface ThirdSvcClient {
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Date.class))
        , description = "",
        headers = {@Header(name = "h1", schema = @Schema(implementation = String.class)),
            @Header(name = "h2", schema = @Schema(implementation = String.class))})
    @RequestMapping(path = "/responseEntity", method = RequestMethod.POST)
    ResponseEntity<Date> responseEntity(@RequestAttribute("date") Date date);
  }

  @Bean
  public RegistryBean thirdRegistryBean() {
    String endpoint;
    if (DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.test.vert.transport", true).get()) {
      endpoint = "rest://localhost:8080?sslEnabled=false&urlPrefix=%2Fapi";
    } else {
      endpoint = "rest://localhost:8080?sslEnabled=false";
    }

    return new RegistryBean().addSchemaInterface("schema-1", ThirdSvcClient.class)
        .setAppId("springmvctest")
        .setServiceName("3rd-svc")
        .setVersion("0.0.1")
        .setInstances(new Instances().setInstances(List.of(new Instance().setEndpoints(List.of(endpoint)))));
  }

  @Bean
  public ThirdSvcClient thirdSvcClient() {
    return Invoker.createProxy("3rd-svc", "schema-1", ThirdSvcClient.class);
  }
}
