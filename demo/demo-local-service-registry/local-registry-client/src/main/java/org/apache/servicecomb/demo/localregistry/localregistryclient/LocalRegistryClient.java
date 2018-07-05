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

package org.apache.servicecomb.demo.localregistry.localregistryclient;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.demo.DemoConst;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.validator.Student;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

public class LocalRegistryClient {
  private static RestTemplate templateNew = RestTemplateBuilder.create();

  public static void main(String[] args) throws Exception {
    System.setProperty("local.registry.file", "src/main/resources/registry.yaml");
    init();

    run();

    TestMgr.summary();
    System.clearProperty("local.registry.file");
  }

  public static void init() throws Exception {
    Log4jUtils.init();
    BeanUtils.init();
  }

  public static void run() throws Exception {
    testLocalRegistry(templateNew);
  }

  private static void testLocalRegistry(RestTemplate template) throws Exception {
    String microserviceName = "localserv";
    for (String transport : DemoConst.transports) {
      CseContext.getInstance().getConsumerProviderManager().setTransport(microserviceName, transport);
      TestMgr.setMsg(microserviceName, transport);

      String cseUrlPrefix = "cse://" + microserviceName + "/localservregistry/";

      Map<String, String> params = new HashMap<>();
      params.put("a", "5");
      params.put("b", "20");
      int result = template.postForObject(cseUrlPrefix + "add", params, Integer.class);
      TestMgr.check(25, result);

      Student student = new Student();
      student.setName("king");
      student.setAge(30);
      Student res = template.postForObject(cseUrlPrefix + "sayhi", student, Student.class);
      TestMgr.check("hello king", res.getName());
      TestMgr.check(30, res.getAge());
    }
  }
}
