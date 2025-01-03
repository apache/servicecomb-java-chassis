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

package org.apache.servicecomb.samples;

import com.google.common.net.HostAndPort;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.foundation.common.utils.ConditionWaiter.SleepUtil;
import org.kiwiproject.consul.Consul;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Component
public class ConsulConfigIT implements CategorizedTestCase {
  RestOperations template = new RestTemplate();

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsulConfigIT.class);

  @Override
  public void testRestTransport() throws Exception {

    testEnvironment();
    testApplication();
    testService();
    testVersion();
    testTag();
    testOverride();
  }

  private void testOverride() {

    putValue("/servicecomb/config/environment/production/application2.properties",
        "testValue=t1");
    putValue("/servicecomb/config/application/production/demo-consul/application2.properties",
        "testValue=t2");
    testGetConfig("testValue", "t2");
    putValue("/servicecomb/config/service/production/demo-consul/provider/application2.properties",
        "testValue=t3");
    testGetConfig("testValue", "t3");
    putValue("/servicecomb/config/version/production/demo-consul/provider/0.0.1/application2.properties",
        "testValue=t4");
    testGetConfig("testValue", "t4");
    putValue("/servicecomb/config/tag/production/demo-consul/provider/0.0.1/tag1/application2.properties",
        "testValue=t5");
    testGetConfig("testValue", "t5");
  }

  private void testEnvironment() {

    putValue("/servicecomb/config/environment/production/application.properties",
        "test1=env");
    putValue("/servicecomb/config/environment/production/application.properties",
        "test1=env1");

    testGetConfig("test1", "env1");
  }


  private void testApplication() {

    putValue("/servicecomb/config/application/production/demo-consul/application.properties",
        "test2=applition");
    putValue("/servicecomb/config/application/production/demo-consul/application.properties",
        "test2=applition2");
    testGetConfig("test2", "applition2");
  }

  private void testService() {

    putValue("/servicecomb/config/service/production/demo-consul/provider/application.properties",
        "test3=service");
    putValue("/servicecomb/config/service/production/demo-consul/provider/application.properties",
        "test3=service3");
    testGetConfig("test3", "service3");
  }

  private void testVersion() {

    putValue("/servicecomb/config/version/production/demo-consul/provider/0.0.1/application.properties",
        "test3=version");
    putValue("/servicecomb/config/version/production/demo-consul/provider/0.0.1/application.properties",
        "test4=version4");
    testGetConfig("test4", "version4");
  }

  private void testTag() {

    putValue("/servicecomb/config/tag/production/demo-consul/provider/0.0.1/tag1/application.properties",
        "test5=tag");
    putValue("/servicecomb/config/tag/production/demo-consul/provider/0.0.1/tag1/application.properties",
        "test5=tag5");
    testGetConfig("test5", "tag5");
  }

  public void putValue(String key, String value) {
    try {
      Consul.Builder builder = Consul.builder().withHostAndPort(HostAndPort.fromParts("127.0.0.1", 8500));
      Consul consulClient = builder.build();
      consulClient.keyValueClient().putValue(key, value);
      LOGGER.info("Value set successfully:{}", value);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void testGetConfig(String key, String expectValue) {

    String result = template
        .getForObject(Config.GATEWAY_URL + "/getConfig?key=" + key, String.class);

    for (int i = 0; i < 4; i++) {
      if (StringUtils.equals(expectValue, result)) {
        TestMgr.check(expectValue, result);
        break;
      }
      SleepUtil.sleep(500, TimeUnit.MILLISECONDS);
    }
  }
}
