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

package org.apache.servicecomb.demo.jaxrs.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.validator.Student;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TestClientTimeout implements CategorizedTestCase {
  private static RestTemplate template = RestTemplateBuilder.create();

  public void testAllTransport() throws Exception {
    testClientTimeOut(template);
  }

  private static void testClientTimeOut(RestTemplate template) {
    String microserviceName = "jaxrs";

    String cseUrlPrefix = "cse://" + microserviceName + "/clientreqtimeout/";

    testClientTimeoutSayHi(template, cseUrlPrefix);
    testClientTimeoutAdd(template, cseUrlPrefix);
  }

  private static void testClientTimeoutSayHi(RestTemplate template, String cseUrlPrefix) {
    Student student = new Student();
    student.setName("timeout");
    student.setAge(30);
    Student result = template.postForObject(cseUrlPrefix + "sayhello", student, Student.class);
    TestMgr.check("hello timeout 30", result);
  }

  private static void testClientTimeoutAdd(RestTemplate template, String cseUrlPrefix) {
    Map<String, String> params = new HashMap<>();
    params.put("a", "5");
    params.put("b", "20");
    boolean failed = false;
//    long failures = 0;
//    ServiceCombServerStats serviceCombServerStats = null;
    try {
//      serviceCombServerStats = getServiceCombServerStats();
//      failures = serviceCombServerStats.getContinuousFailureCount();
      template.postForObject(cseUrlPrefix + "add", params, Integer.class);
    } catch (InvocationException e) {
      failed = true;
      // implement timeout with same error code and message for rest and highway
      TestMgr.check(408, e.getStatus().getStatusCode());
      // Request Timeout or Invocation Timeout
      TestMgr.check(true,
          e.getErrorData().toString().contains("Timeout."));
    }

    TestMgr.check(true, failed);
  }
}
