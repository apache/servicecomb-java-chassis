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

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ConsumerConfigIT implements CategorizedTestCase {
  RestTemplate template = new RestTemplate();

  @Override
  public void testRestTransport() throws Exception {
    testConfig();
    testFooBar();
  }

  private void testConfig() {
    String result = template.getForObject(Config.GATEWAY_URL + "/config?key=cse.v2.test.foo", String.class);
    TestMgr.check("\"foo\"", result);
    result = template.getForObject(Config.GATEWAY_URL + "/config?key=cse.v2.test.bar", String.class);
    TestMgr.check("\"bar\"", result);
    result = template.getForObject(Config.GATEWAY_URL + "/config?key=cse.v2.test.priority", String.class);
    TestMgr.check("\"v3\"", result);
    result = template.getForObject(Config.GATEWAY_URL + "/config?key=cse.v2.test.common", String.class);
    TestMgr.check("\"common\"", result);
  }

  private void testFooBar() {
    String result = template.getForObject(Config.GATEWAY_URL + "/bar", String.class);
    TestMgr.check("\"bar\"", result);
    result = template.getForObject(Config.GATEWAY_URL + "/foo", String.class);
    TestMgr.check("\"foo\"", result);
    result = template.getForObject(Config.GATEWAY_URL + "/priority", String.class);
    TestMgr.check("\"v3\"", result);
    result = template.getForObject(Config.GATEWAY_URL + "/common", String.class);
    TestMgr.check("\"common\"", result);
  }
}
