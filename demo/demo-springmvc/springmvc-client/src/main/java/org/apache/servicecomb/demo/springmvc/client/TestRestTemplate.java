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

import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

public class TestRestTemplate {
  private RestTemplate restTemplate = RestTemplateBuilder.create();

  public void runAllTest() {
    testvoidResponse();
    testVoidResponse();
    checkAllVoidTestResult();
  }

  private void testvoidResponse() {
    final ResponseEntity<Void> resultEntity = restTemplate
        .getForEntity("cse://springmvc/codeFirstSpringmvc/testvoidInRestTemplate", void.class);
    Assert.isTrue(200 == resultEntity.getStatusCodeValue(), "void return type invocation failed");
  }

  private void testVoidResponse() {
    final ResponseEntity<Void> resultEntity = restTemplate
        .getForEntity("cse://springmvc/codeFirstSpringmvc/testVoidInRestTemplate", Void.class);
    Assert.isTrue(200 == resultEntity.getStatusCodeValue(), "Void return type invocation failed");
  }

  private void checkAllVoidTestResult() {
    final ResponseEntity<Boolean> resultEntity = restTemplate
        .getForEntity("cse://springmvc/codeFirstSpringmvc/checkVoidResult", boolean.class);
    Assert.isTrue(resultEntity.getBody(), "not all void test is passed");
  }
}
