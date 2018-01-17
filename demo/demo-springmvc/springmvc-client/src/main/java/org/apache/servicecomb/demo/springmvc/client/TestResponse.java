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

import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.swagger.invocation.Response;
import org.springframework.http.ResponseEntity;

public class TestResponse {
  private CodeFirstSprigmvcIntf intf;

  public TestResponse() {
    intf = Invoker.createProxy("springmvc", "codeFirst", CodeFirstSprigmvcIntf.class);
  }

  public void runRest() {
  }

  public void runHighway() {
  }

  public void runAllTransport() {
    testResponseEntity();
    testCseResponse();
  }

  private void testCseResponse() {
    String srcName = RegistryUtils.getMicroservice().getServiceName();
    Response cseResponse = intf.cseResponse();
    TestMgr.check("User [name=nameA, age=100, index=0]", cseResponse.getResult());
    TestMgr.check("h1v " + srcName, cseResponse.getHeaders().getFirst("h1"));
    TestMgr.check("h2v " + srcName, cseResponse.getHeaders().getFirst("h2"));
  }

  private void testResponseEntity() {
    Date date = new Date();

    String srcName = RegistryUtils.getMicroservice().getServiceName();

    ResponseEntity<Date> responseEntity = intf.responseEntity(date);
    TestMgr.check(date, responseEntity.getBody());
    TestMgr.check("h1v " + srcName, responseEntity.getHeaders().getFirst("h1"));
    TestMgr.check("h2v " + srcName, responseEntity.getHeaders().getFirst("h2"));

    TestMgr.check(202, responseEntity.getStatusCode());
  }
}
