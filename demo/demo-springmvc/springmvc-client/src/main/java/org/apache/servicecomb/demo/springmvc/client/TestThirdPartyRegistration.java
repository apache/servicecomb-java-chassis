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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.apache.servicecomb.registry.RegistrationManager;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TestThirdPartyRegistration implements BootListener, CategorizedTestCase {
  private ThirdPartyService thirdPartyService;

  @Override
  public void onAfterRegistry(BootEvent event) {
    List<String> endpoints = new ArrayList<>();
    endpoints.add("rest://localhost:8080");
    RegistrationManager.INSTANCE.registerMicroserviceMappingByEndpoints("testServiceName",
        "1.0.1", endpoints, ThirdPartyService.class);
    thirdPartyService = Invoker.createProxy("testServiceName",
        "testServiceName", ThirdPartyService.class);
  }

  @Override
  public void testRestTransport() throws Exception {
    Date date = new Date();
    ResponseEntity<Date> responseEntity = thirdPartyService.responseEntity(date);
    TestMgr.check(date, responseEntity.getBody());
    // Third party invocation do not pass cse-context to the target
    TestMgr.check("h1v null", responseEntity.getHeaders().getFirst("h1"));
    TestMgr.check("h2v null", responseEntity.getHeaders().getFirst("h2"));

    TestMgr.check(202, responseEntity.getStatusCodeValue());
  }

  @Override
  public String getMicroserviceName() {
    return "testServiceName";
  }
}
