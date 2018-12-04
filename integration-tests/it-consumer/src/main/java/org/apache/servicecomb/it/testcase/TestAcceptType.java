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

import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class TestAcceptType {
  interface AcceptTypeIntf {
  }

  private static Consumers<AcceptTypeIntf> consumersAcceptTypeSpringmvc = new Consumers<>("acceptTypeSpringmvcSchema",
      AcceptTypeIntf.class);

  private static Consumers<AcceptTypeIntf> consumersAcceptTypeJaxrs = new Consumers<>("acceptTypeJaxrsSchema",
      AcceptTypeIntf.class);

  @Test
  public void testTextPlain_rt() {
    checkTextPlain(consumersAcceptTypeSpringmvc);
    checkTextPlain(consumersAcceptTypeJaxrs);
  }

  private void checkTextPlain(Consumers<AcceptTypeIntf> consumers) {
    String result = textHeader_rt(consumers, MediaType.TEXT_PLAIN_VALUE);
    Assert.assertEquals("cse", result);
    try {
      textHeader_rt(consumers, MediaType.APPLICATION_JSON_VALUE);
      Assert.fail("should throw exception");
    } catch (InvocationException e) {
      Assert.assertEquals(406, e.getStatusCode());
      Assert.assertTrue(e.getMessage().contains("Accept application/json is not supported"));
    } catch (Throwable e) {
      Assert.fail(" should throw InvocationException");
    }
  }

  private String textHeader_rt(Consumers<AcceptTypeIntf> consumers, String type) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("accept", type);
    HttpEntity<?> entity = new HttpEntity<>(headers);
    ResponseEntity<String> response = consumers.getSCBRestTemplate()
        .exchange("/sayHi?name=cse",
            HttpMethod.GET,
            entity,
            String.class);
    return response.getBody();
  }

  @Test
  public void testProducerApplicationJson_rt() {
    checkApplicationJson(consumersAcceptTypeSpringmvc);
    checkApplicationJson(consumersAcceptTypeJaxrs);
  }

  private void checkApplicationJson(Consumers<AcceptTypeIntf> consumers) {
    String result = jsonHeader_rt(consumers, MediaType.APPLICATION_JSON_VALUE);
    Assert.assertEquals("cse", result);
    try {
      jsonHeader_rt(consumersAcceptTypeSpringmvc, MediaType.TEXT_PLAIN_VALUE);
      Assert.fail("should throw exception");
    } catch (InvocationException e) {
      Assert.assertEquals(406, e.getStatusCode());
      Assert.assertTrue(e.getMessage().contains("Accept text/plain is not supported"));
    } catch (Throwable e) {
      Assert.fail("should throw InvocationException");
    }
  }

  private String jsonHeader_rt(Consumers<AcceptTypeIntf> consumers, String type) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("accept", type);
    HttpEntity<?> entity = new HttpEntity<>(headers);
    ResponseEntity<String> response = consumers.getSCBRestTemplate()
        .exchange("/sayHello?name=cse",
            HttpMethod.GET,
            entity,
            String.class);
    return response.getBody();
  }
}
