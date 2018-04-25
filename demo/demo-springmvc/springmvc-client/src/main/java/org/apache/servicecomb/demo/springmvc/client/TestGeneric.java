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

import java.util.Date;

import javax.xml.ws.Holder;

import org.apache.servicecomb.demo.Generic;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.apache.servicecomb.provider.springmvc.reference.CseRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

public class TestGeneric {
  private CodeFirstSpringmvcIntf intf;

  private RestTemplate restTemplate = new CseRestTemplate();

  private String prefix = "cse://springmvc/codeFirstSpringmvc";

  public TestGeneric() {
    intf = Invoker.createProxy("springmvc", "codeFirst", CodeFirstSpringmvcIntf.class);
  }

  public void runRest() {
    testHolderUser_rest();
    testGenericUser_rest();
    testGenericGenericUser_rest();
    testGenericLong_rest();
    testGenericDate_rest();
    testGenericEnum_rest();
  }

  public void runHighway() {
  }

  public void runAllTransport() {
  }

  @SuppressWarnings("unchecked")
  private void testGenericEnum_rest() {
    Generic<HttpStatus> generic = new Generic<>();
    generic.value = HttpStatus.OK;
    Generic<HttpStatus> result = intf.testGenericEnum(generic);
    TestMgr.check(HttpStatus.OK, result.value);
    TestMgr.check(HttpStatus.class, result.value.getClass());

    result = restTemplate.postForObject(prefix + "/genericEnum", generic, Generic.class);
    TestMgr.check(HttpStatus.OK, result.value);
    TestMgr.check(HttpStatus.class, result.value.getClass());
  }

  @SuppressWarnings({"unchecked", "deprecation"})
  private void testGenericDate_rest() {
    Generic<Date> generic = new Generic<>();
    generic.value = new Date(1001);
    Generic<Date> result = intf.testGenericDate(generic);
    TestMgr.check("1970-01-01T00:00:01.001Z",
        com.fasterxml.jackson.databind.util.ISO8601Utils.format(result.value, true));
    TestMgr.check(Date.class, result.value.getClass());

    result = restTemplate.postForObject(prefix + "/genericDate", generic, Generic.class);
    TestMgr.check("1970-01-01T00:00:01.001Z",
        com.fasterxml.jackson.databind.util.ISO8601Utils.format(result.value, true));
    TestMgr.check(Date.class, result.value.getClass());
  }

  @SuppressWarnings("unchecked")
  private void testGenericLong_rest() {
    Generic<Long> generic = new Generic<>();
    generic.value = 100L;
    Generic<Long> result = intf.testGenericLong(generic);
    TestMgr.check(100, result.value);
    TestMgr.check(Long.class, result.value.getClass());

    result = restTemplate.postForObject(prefix + "/genericLong", generic, Generic.class);
    TestMgr.check(100, result.value);
    TestMgr.check(Long.class, result.value.getClass());
  }

  @SuppressWarnings("unchecked")
  private void testGenericGenericUser_rest() {
    Generic<Generic<User>> generic = new Generic<>();
    generic.value = new Generic<>();
    generic.value.value = new User();
    Generic<Generic<User>> result = intf.testGenericGenericUser(generic);
    TestMgr.check("{\"name\":\"nameA\",\"age\":100,\"index\":0,\"names\":null}", result.value.value.jsonString());

    result = restTemplate.postForObject(prefix + "/genericGenericUser", generic, Generic.class);
    TestMgr.check("{\"name\":\"nameA\",\"age\":100,\"index\":0,\"names\":null}", result.value.value.jsonString());
  }

  @SuppressWarnings("unchecked")
  private void testGenericUser_rest() {
    Generic<User> generic = new Generic<>();
    generic.value = new User();
    Generic<User> result = intf.testGenericUser(generic);
    TestMgr.check("{\"name\":\"nameA\",\"age\":100,\"index\":0,\"names\":null}", result.value.jsonString());

    result = restTemplate.postForObject(prefix + "/genericUser", generic, Generic.class);
    TestMgr.check("{\"name\":\"nameA\",\"age\":100,\"index\":0,\"names\":null}", result.value.jsonString());
  }

  @SuppressWarnings("unchecked")
  private void testHolderUser_rest() {
    Holder<User> holder = new Holder<>(new User());
    Holder<User> result = intf.testHolderUser(holder);
    TestMgr.check("{\"name\":\"nameA\",\"age\":100,\"index\":0,\"names\":null}", result.value.jsonString());

    result = restTemplate.postForObject(prefix + "/holderUser", holder, Holder.class);
    TestMgr.check("{\"name\":\"nameA\",\"age\":100,\"index\":0,\"names\":null}", result.value.jsonString());
  }
}
