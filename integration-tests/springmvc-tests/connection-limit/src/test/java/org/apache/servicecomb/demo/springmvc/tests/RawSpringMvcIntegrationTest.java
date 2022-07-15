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

package org.apache.servicecomb.demo.springmvc.tests;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.web.client.RestTemplate;

public class RawSpringMvcIntegrationTest {

  private RestTemplate restTemplate = RestTemplateBuilder.create();

  private final String baseUrl = "http://127.0.0.1:8080/";

  private final String controllerUrl = baseUrl + "springmvc/controller/";

  @BeforeAll
  public static void init() throws Exception {
    System.setProperty("servicecomb.uploads.directory", "/tmp");
    SpringMvcTestMain.main(new String[0]);
  }

  @Test
  public void ensureServerWorksFine() {
    try {
      restTemplate.getForObject(controllerUrl + "sayhi?name=world", String.class);
      Assertions.fail("connection limit failed");
    } catch (Exception ex) {
      Assertions.assertEquals("java.net.SocketException: Unexpected end of file from server", ex.getCause().toString());
    }
  }

  @AfterAll
  public static void shutdown() {
    SCBEngine.getInstance().destroy();
  }
}
