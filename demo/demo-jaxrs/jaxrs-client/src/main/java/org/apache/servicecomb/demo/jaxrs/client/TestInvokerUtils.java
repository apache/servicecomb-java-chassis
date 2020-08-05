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

import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.server.User;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

@Component
public class TestInvokerUtils implements CategorizedTestCase {
  private static final String SERVICE_NAME = "jaxrs";

  private static final String SCHEMA_ID = "codeFirst";

  public void testAllTransport() throws Exception {
    testCodeFirstJaxrs();
  }

  // invoke CodeFirstJaxrs
  private void testCodeFirstJaxrs() {
    Map<String, Object> swaggerArguments = new HashMap<>();
    Map<String, User> userMap = new HashMap<>();
    User user = new User();
    user.setName("hello");
    userMap.put("user", user);
    swaggerArguments.put("userMap", userMap);
    TypeReference<Map<String, User>> type = new TypeReference<Map<String, User>>() {
    };
    Map<String, User> result = InvokerUtils.syncInvoke(SERVICE_NAME, SCHEMA_ID, "testUserMap", swaggerArguments,
        type.getType());
    TestMgr.check(result.get("user").getName(), userMap.get("user").getName());
  }
}
