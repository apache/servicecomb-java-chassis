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

package org.apache.servicecomb.demo.registry;

import java.util.List;
import java.util.Map;

import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.springboot2.starter.EnableServiceComb;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableServiceComb
@Component
public class Application {
  @RpcReference(microserviceName = "thirdParty-service-center", schemaId = "ServiceCenterEndpoint")
  static IServiceCenterEndpoint serviceCenterEndpoint;

  public static void main(final String[] args) throws Exception {
    new SpringApplicationBuilder().sources(Application.class).web(WebApplicationType.SERVLET).build().run(args);

    runTest();
  }

  public static void runTest() {
    RestTemplate template = RestTemplateBuilder.create();

    // invoke demo-multi-registries-server
    TestMgr.check("2", template
        .getForObject("cse://demo-multi-registries-server/register/url/prefix/getName?name=2",
            String.class));

    // invoke service-center(3rd-parties)
    @SuppressWarnings("unchecked")
    Map<String, List<?>> result = (Map<String, List<?>>) serviceCenterEndpoint.getInstances("demo-multi-registries",
        "demo-multi-registries-server",
        "true",
        "0.0.2",
        "default");
    TestMgr.check(result.get("instances").size(), 1);
    TestMgr.summary();
    if (!TestMgr.errors().isEmpty()) {
      throw new IllegalStateException("tests failed");
    }
  }
}
