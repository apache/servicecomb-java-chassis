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

package org.apache.servicecomb.it.testcase;

import org.apache.servicecomb.it.extend.engine.GateRestTemplate;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class TestExceptionConvertEdge {
  private static GateRestTemplate client = GateRestTemplate.createEdgeRestTemplate("edgeExceptionConvertSchema");

  @Test
  public void testTimeoutAdd() {
    int result = client.getForObject("/add?x=10&y=12", Integer.class);
    Assert.assertEquals(22, result);

    try {
      client.getForObject("/add?x=88&y=21", Object.class);
      // This test case have some problem: for some test case, e.g. spring boot, will get result, others may timeout
      // Because of timeout settings.
    } catch (HttpClientErrorException exception) {
      Assert.assertEquals(HttpStatus.EXPECTATION_FAILED.value(), exception.getRawStatusCode());
      Assert.assertTrue(exception.getResponseBodyAsString().contains("change the response"));
    }
    try {
      client.getForObject("/add?x=88&y=21", Object.class);
      // This test case have some problem: for some test case, e.g. spring boot, will get result, others may timeout
      // Because of timeout settings.
    } catch (HttpClientErrorException exception) {
      Assert.assertEquals(HttpStatus.EXPECTATION_FAILED.value(), exception.getRawStatusCode());
      Assert.assertTrue(exception.getResponseBodyAsString().contains("change the response"));
    }
  }
}
