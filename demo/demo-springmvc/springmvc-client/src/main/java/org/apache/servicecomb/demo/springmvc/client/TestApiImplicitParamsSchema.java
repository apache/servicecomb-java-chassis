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

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

@Component
public class TestApiImplicitParamsSchema implements CategorizedTestCase {
  private final RestOperations restOperations = RestTemplateBuilder.create();

  @Override
  public void testRestTransport() throws Exception {
    testImplicitAndExplicitParam();
    testIntegerTypeValidation();
  }

  private void testIntegerTypeValidation() throws URISyntaxException {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add("x-test-a", "a");
    headers.add("x-test-b", "30");
    headers.add("x-test-c", "x");  // invalid integer for object-mapper.
    RequestEntity<?> entity = new RequestEntity<>(headers, HttpMethod.GET,
        new URI("servicecomb://springmvc/implicit/testIntegerTypeValidation"));
    try {
      String result = restOperations.exchange(entity, String.class).getBody();
      TestMgr.check(result, "do not have integer type check");
    } catch (InvocationException e) {
      TestMgr.check(e.getStatusCode(), 400);
      TestMgr.check(e.getMessage().contains("x-test-c"), true);
    }
  }

  private void testImplicitAndExplicitParam() throws URISyntaxException {
    // test all parameters case
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add("x-test-a", "a");
    headers.add("x-test-b", "30");
    RequestEntity<?> entity = new RequestEntity<>(headers, HttpMethod.GET,
        new URI("servicecomb://springmvc/implicit/testImplicitAndExplicitParam?a=1&b=2"));
    String result = restOperations.exchange(entity, String.class).getBody();
    TestMgr.check("a,30,3", result);

    // test default value
    headers = new LinkedMultiValueMap<>();
    headers.add("x-test-b", "10");
    entity = new RequestEntity<>(headers, HttpMethod.GET,
        new URI("servicecomb://springmvc/implicit/testImplicitAndExplicitParam?a=1&b=2"));
    result = restOperations.exchange(entity, String.class).getBody();
    TestMgr.check("test,10,3", result);

    // test default required check
    try {
      result = restOperations.getForObject("servicecomb://springmvc/implicit/testImplicitAndExplicitParam?a=1&b=2",
          String.class);
      TestMgr.check(result, "do not have required check");
    } catch (InvocationException e) {
      TestMgr.check(e.getStatusCode(), 400);
      TestMgr.check(e.getMessage().contains("x-test-b"), true);
    }
  }
}
