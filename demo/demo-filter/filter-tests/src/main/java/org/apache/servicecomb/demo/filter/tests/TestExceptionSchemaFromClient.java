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
package org.apache.servicecomb.demo.filter.tests;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TestExceptionSchemaFromClient implements CategorizedTestCase {

  RestTemplate restTemplate = RestTemplateBuilder.create();

  private static final String SERVER = "servicecomb://filterClient";

  @Override
  public String getMicroserviceName() {
    return "filterEdge";
  }

  @Override
  public void testAllTransport() throws Exception {
    testBlockingExceptionRestTemplate();
    testBlockingExceptionReference();
    testBlockingExceptionInvoker();
    testReactiveExceptionReference();
  }

  private void testBlockingExceptionRestTemplate() {
    try {
      restTemplate.getForObject(SERVER + "/exception/blockingExceptionRestTemplate",
          boolean.class);
    } catch (InvocationException e) {
      TestMgr.check(503, e.getStatus().getStatusCode());
      TestMgr.check("Blocking Exception", ((CommonExceptionData) e.getErrorData()).getMessage());
    }
  }

  private void testBlockingExceptionReference() {
    try {
      restTemplate.getForObject(SERVER + "/exception/blockingExceptionReference",
          boolean.class);
    } catch (InvocationException e) {
      TestMgr.check(503, e.getStatus().getStatusCode());
      TestMgr.check("Blocking Exception", ((CommonExceptionData) e.getErrorData()).getMessage());
    }
  }

  private void testBlockingExceptionInvoker() {
    try {
      restTemplate.getForObject(SERVER + "/exception/blockingExceptionInvoker",
          boolean.class);
    } catch (InvocationException e) {
      TestMgr.check(503, e.getStatus().getStatusCode());
      TestMgr.check("Blocking Exception", ((CommonExceptionData) e.getErrorData()).getMessage());
    }
  }

  private void testReactiveExceptionReference() {
    try {
      restTemplate.getForObject(SERVER + "/exception/reactiveExceptionReference",
          boolean.class);
    } catch (InvocationException e) {
      TestMgr.check(503, e.getStatus().getStatusCode());
      TestMgr.check("Reactive Exception", ((CommonExceptionData) e.getErrorData()).getMessage());
    }
  }
}
