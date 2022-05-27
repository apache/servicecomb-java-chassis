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
import org.apache.servicecomb.loadbalance.ServiceCombLoadBalancerStats;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.loadbalance.ServiceCombServerStats;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.cache.InstanceCache;
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
      // TODO: 这个测试用例失败不会影响当前功能。 需要在完成 SCB-2213重试重构、实例统计状态基于事件重构（当前
      //  在LoadbalanceHandler进行实例统计信息更新，应该基于服务执行完成事件更新，重试也应该在调用层重试。）
      // 等功能后，才能够启用这个测试用例检查。
      // TestMgr.check(serviceCombServerStats.getContinuousFailureCount(), failures + 1);
    }

    TestMgr.check(true, failed);
  }

  private static ServiceCombServerStats getServiceCombServerStats() {
    InstanceCache instanceCache = DiscoveryManager.INSTANCE.getInstanceCacheManager()
        .getOrCreate(RegistrationManager.INSTANCE.getAppId(),
            "jaxrs", "0+");
    org.apache.servicecomb.registry.api.registry.MicroserviceInstance microserviceInstance = instanceCache
        .getInstanceMap().values().iterator().next();
    ServiceCombServer serviceCombServer = ServiceCombLoadBalancerStats.INSTANCE
        .getServiceCombServer(microserviceInstance);
    return ServiceCombLoadBalancerStats.INSTANCE
        .getServiceCombServerStats(serviceCombServer);
  }
}
