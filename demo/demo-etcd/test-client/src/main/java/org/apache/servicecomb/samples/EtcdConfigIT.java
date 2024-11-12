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

import java.nio.charset.StandardCharsets;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;

@Component
public class EtcdConfigIT implements CategorizedTestCase {
  RestOperations template = new RestTemplate();

  private static final Logger LOGGER = LoggerFactory.getLogger(EtcdConfigIT.class);

  @Override
  public void testRestTransport() throws Exception {

    testEnvironment();
    testApplication();
    testService();
    testVersion();
    testTag();
  }

  private void testEnvironment() {

    putValue("/servicecomb/config/environment/production/application.properties",
        "test1=env1");
    testGetConfig("test1", "env1");
  }

  private void testApplication() {

    putValue("/servicecomb/config/environment/production/demo-etcd/application.properties",
        "test2=applition2");
    testGetConfig("test2", "applition2");
  }

  private void testService() {

    putValue("/servicecomb/config/environment/production/demo-etcd/test-client/application.properties",
        "test3=service3");
    testGetConfig("test3", "service3");
  }

  private void testVersion() {

    putValue("/servicecomb/config/environment/production/demo-etcd/test-client/0.0.1/application.properties",
        "test4=version4");
    testGetConfig("test4", "version4");
  }

  private void testTag() {

    putValue("/servicecomb/config/environment/production/demo-etcd/test-client/0.0.1/tag1/application.properties",
        "test5=tag5");
    testGetConfig("test5", "tag5");
  }


  public void putValue(String key, String value) {
    try (Client client = Client.builder().endpoints("http://localhost:2379").build()) {

      client.getKVClient().put(
          ByteSequence.from(key, StandardCharsets.UTF_8),
          ByteSequence.from(value, StandardCharsets.UTF_8)
      ).get();

      LOGGER.info("Value set successfully");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void testGetConfig(String key, String expectValue) {

    String result = template
        .getForObject(Config.GATEWAY_URL + "/getConfig?key=" + key, String.class);
    TestMgr.check(expectValue, result);
  }
}
