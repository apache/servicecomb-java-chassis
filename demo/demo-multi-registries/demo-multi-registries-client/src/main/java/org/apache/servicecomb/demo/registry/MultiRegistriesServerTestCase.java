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

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MultiRegistriesServerTestCase implements CategorizedTestCase {
  RestTemplate template = RestTemplateBuilder.create();

  private DiscoveryManager discoveryManager;

  @Autowired
  public void setDiscoveryManager(DiscoveryManager discoveryManager) {
    this.discoveryManager = discoveryManager;
  }

  @Override
  public void testRestTransport() throws Exception {
    testServerGetName();
    testGetAllMicroservice();
  }

  private void testGetAllMicroservice() {
    List<? extends DiscoveryInstance> microserviceList = discoveryManager
        .findServiceInstances("demo-multi-registries", "demo-multi-registries-client");
    TestMgr.check(1, microserviceList.size());
    microserviceList = discoveryManager
        .findServiceInstances("demo-multi-registries", "demo-multi-registries-server");
    TestMgr.check(1, microserviceList.size());
    microserviceList = discoveryManager
        .findServiceInstances("demo-multi-registries", "thirdParty-service-center");
    TestMgr.check(1, microserviceList.size());
    microserviceList = discoveryManager
        .findServiceInstances("demo-multi-registries", "thirdParty-no-schema-server");
    TestMgr.check(1, microserviceList.size());
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
