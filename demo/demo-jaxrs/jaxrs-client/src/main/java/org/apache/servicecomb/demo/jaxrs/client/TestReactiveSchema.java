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

import java.util.Map;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

@Component
public class TestReactiveSchema implements CategorizedTestCase {
  @SuppressWarnings("rawtypes")
  public void testRestTransport() throws Exception {
    RestOperations restTemplate = RestTemplateBuilder.create();
    try {
      restTemplate.getForObject("cse://jaxrs/reactive/testSyncInvokeInEventLoop?a=1&b=2", int.class);
      TestMgr.check(true, false);
    } catch (InvocationException e) {
      TestMgr.check(e.getStatusCode(), 500);
      TestMgr.check(((Map) e.getErrorData()).get("message"),
          "Unexpected exception when processing jaxrs.ReactiveSchema.testSyncInvokeInEventLoop. "
              + "Can not execute sync logic in event loop.");
    }
  }
}
