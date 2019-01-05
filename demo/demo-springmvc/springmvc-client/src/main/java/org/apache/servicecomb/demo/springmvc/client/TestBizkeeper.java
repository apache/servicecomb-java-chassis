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

import java.util.concurrent.TimeoutException;

import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.web.client.RestTemplate;

public class TestBizkeeper {
  private RestTemplate restTemplate = RestTemplateBuilder.create();

  private String prefix = "cse://springmvc/codeFirstBizkeeperTest";

  public void runAllTest() {
    testTimeout();
  }

  private void testTimeout() {
    //config timeout starts with cse.xxx, from yaml
    try {
      restTemplate.getForObject(prefix + "/testTimeout?name={1}&delaytime={2}", String.class, "joker", 1000);
      TestMgr.check("expect: throw timeout exception", "real: not throw timeout exception");
    } catch (Exception e) {
      TestMgr.check(TimeoutException.class, e.getCause().getCause().getClass());
    }
    //modify config timeout starts with servicecomb.xxx, can change the timeout effect
    ArchaiusUtils.setProperty(
        "servicecomb.isolation.Consumer.springmvc.codeFirstBizkeeperTest.testTimeout.timeout.enabled", "false");
    try {
      String result =
          restTemplate.getForObject(prefix + "/testTimeout?name={1}&delaytime={2}", String.class, "joker", 1000);
      TestMgr.check("joker", result);
    } catch (Exception e) {
      TestMgr.check("expect: not throw timeout exception", "real: throw timeout exception");
    }

    try {
      ArchaiusUtils.setProperty(
          "servicecomb.fallback.Consumer.springmvc.codeFirstBizkeeperTest.testTimeout.enabled", "false");
      ArchaiusUtils.setProperty(
          "servicecomb.isolation.Consumer.springmvc.codeFirstBizkeeperTest.testTimeout.timeout.enabled", "true");
      ArchaiusUtils.setProperty(
          "servicecomb.isolation.Consumer.springmvc.codeFirstBizkeeperTest.testTimeout.timeoutInMilliseconds", 500);
      restTemplate.getForObject(prefix + "/testTimeout?name={1}&delaytime={2}", String.class, "joker", 1000);
      TestMgr.check("expect: throw timeout exception", "real: not throw timeout exception");
    } catch (InvocationException e) {
      TestMgr.check(TimeoutException.class, e.getCause().getCause().getClass());
    }
  }
}
