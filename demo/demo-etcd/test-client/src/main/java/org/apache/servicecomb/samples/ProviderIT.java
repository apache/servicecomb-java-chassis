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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Component
public class ProviderIT implements CategorizedTestCase {

  RestOperations template = new RestTemplate();

  private static final Logger LOGGER = LoggerFactory.getLogger(ProviderIT.class);

  @Override
  public void testRestTransport() throws Exception {

    User user = getUser("Application/json");
    TestMgr.check(1L, user.getId());
    TestMgr.check("czd", user.getName());

    User user2 = getUser("application/json");
    TestMgr.check(1L, user2.getId());
    TestMgr.check("czd", user2.getName());

    User user3 = getUser("APPLICATION/JSON");
    TestMgr.check(1L, user3.getId());
    TestMgr.check("czd", user3.getName());
  }

  private User getUser(String contentType) throws IOException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", contentType);

    String requestBody = """
        {
            "id": 1,
            "name": "czd"
        }
        """;

    HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

    String url = Config.GATEWAY_URL + "/testContentType";
    ResponseEntity<String> response = template.exchange(
        url, HttpMethod.POST, entity, String.class);

    User user = JsonUtils.readValue(response.getBody().getBytes(StandardCharsets.UTF_8), User.class);
    return user;
  }
}
