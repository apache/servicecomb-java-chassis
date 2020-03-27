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

package org.apache.servicecomb.it.testcase.publicHeaders;

import static org.junit.Assert.assertEquals;

import org.apache.servicecomb.it.extend.engine.GateRestTemplate;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class TestPublicHeadersEdge {
  static GateRestTemplate jaxClient = GateRestTemplate.createEdgeRestTemplate("edgePublicHeadersJaxrsSchema");

  static GateRestTemplate springMvcClient = GateRestTemplate.createEdgeRestTemplate("edgePublicHeadersSpringMVCSchema");

  private static final String expectHeaderTest = "x_cse_test";

  private static final String expectExternal1 = "external_1";

  private static final String expectExternal2 = "external_2";

  private static final String external3 = "external_3";

  @Test
  public void testJaxrsClientPublicRequestHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("x_cse_test", expectHeaderTest);
    headers.add("external_1", expectExternal1);
    headers.add("external_2", expectExternal2);
    headers.add("external_3", external3);

    HttpEntity<?> entity = new HttpEntity<>(headers);
    ResponseEntity<String> response = jaxClient.exchange("/requestHeaders",
        HttpMethod.GET,
        entity,
        String.class);
    assertEquals(expectHeaderTest + "_" + expectExternal1 + "_" + expectExternal2, response.getBody());
  }

  @Test
  public void testSpringMvcClientPublicRequestHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("x_cse_test", expectHeaderTest);
    headers.add("external_1", expectExternal1);
    headers.add("external_2", expectExternal2);
    headers.add("external_3", external3);

    HttpEntity<?> entity = new HttpEntity<>(headers);
    ResponseEntity<String> response = springMvcClient.exchange("/requestHeaders",
        HttpMethod.GET,
        entity,
        String.class);
    assertEquals(expectHeaderTest + "_" + expectExternal1 + "_" + expectExternal2, response.getBody());
  }
}
