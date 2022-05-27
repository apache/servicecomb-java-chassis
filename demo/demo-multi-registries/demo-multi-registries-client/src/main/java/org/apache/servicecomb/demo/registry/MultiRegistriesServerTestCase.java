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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MultiRegistriesServerTestCase implements CategorizedTestCase {
  RestTemplate template = RestTemplateBuilder.create();

  @Override
  public void testRestTransport() throws Exception {
    testServerGetName();
    testGetAllMicroservice();
  }

  private void testGetAllMicroservice() {
    List<Microservice> microserviceList = DiscoveryManager.INSTANCE.getAllMicroservices();
    Set<String> names = new HashSet<>();

    for (Microservice m : microserviceList) {
      if (m.getServiceName().equals("demo-multi-registries-client")
          || m.getServiceName().equals("demo-multi-registries-server")
          || m.getServiceName().equals("thirdParty-service-center")
          || m.getServiceName().equals("thirdParty-no-schema-server")) {
        names.add(m.getServiceName());
      }
    }
    TestMgr.check(4, names.size());
  }

  private void testServerGetName() {
    // invoke demo-multi-registries-server
    TestMgr.check("2", template
        .getForObject("cse://demo-multi-registries-server/register/url/prefix/getName?name=2",
            String.class));
  }

  @Override
  public void testHighwayTransport() throws Exception {

  }

  @Override
  public void testAllTransport() throws Exception {

  }
}
