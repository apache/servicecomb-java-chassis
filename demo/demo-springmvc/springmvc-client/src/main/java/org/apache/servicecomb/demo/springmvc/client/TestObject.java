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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.Holder;

import org.apache.servicecomb.demo.EmptyObject;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.apache.servicecomb.provider.springmvc.reference.CseRestTemplate;
import org.springframework.web.client.RestTemplate;

public class TestObject {
  private CodeFirstSpringmvcIntf intf;

  private RestTemplate restTemplate = new CseRestTemplate();

  private String prefix = "cse://springmvc/codeFirstSpringmvc";

  public TestObject() {
    intf = Invoker.createProxy("springmvc", "codeFirst", CodeFirstSpringmvcIntf.class);
  }

  public void runRest() {
    testEmptyObject_rest();
    testMapObject_rest();
    testObject();
    testListObject();
    testHolderObject();
  }

  public void runHighway() {
    testEmptyObject_highway();
    testMapObject_highway();
  }

  public void runAllTransport() {
    // TODO : WEAK not supported now in HIGHWAY
    testObject();
//    testListObject();
//    testHolderObject();
  }

  @SuppressWarnings("unchecked")
  private void testHolderObject() {
    Holder<Object> holder = new Holder<>("v");
    Holder<Object> result = intf.testHolderObject(holder);
    TestMgr.check("v", result.value);

    result = restTemplate.postForObject(prefix + "/holderObject", holder, Holder.class);
    TestMgr.check("v", result.value);
  }

  @SuppressWarnings("unchecked")
  private void testListObject() {
    List<Object> list = Collections.singletonList("v");
    List<Object> result = intf.testListObject(list);
    TestMgr.check("[v]", result);
    TestMgr.check(ArrayList.class, result.getClass());

    result = restTemplate.postForObject(prefix + "/listObject", list, List.class);
    TestMgr.check("[v]", result);
    TestMgr.check(ArrayList.class, result.getClass());
  }

  @SuppressWarnings("unchecked")
  private void testMapObject_rest() {
    Map<String, Object> map = Collections.singletonMap("k", "v");
    Map<String, Object> result = intf.testMapObject(map);
    TestMgr.check("{k=v}", result);
    TestMgr.check(LinkedHashMap.class, result.getClass());

    result = restTemplate.postForObject(prefix + "/mapObject", map, Map.class);
    TestMgr.check("{k=v}", result);
    TestMgr.check(LinkedHashMap.class, result.getClass());
  }

  @SuppressWarnings("unchecked")
  private void testMapObject_highway() {
    Map<String, Object> map = Collections.singletonMap("k", "v");
    Map<String, Object> result = intf.testMapObject(map);
    TestMgr.check("{k=v}", result);
    // This is a behavior change in 2.0.0, before 2.0.0 runtime type of RAW is HashMap
    TestMgr.check(LinkedHashMap.class, result.getClass());

    result = restTemplate.postForObject(prefix + "/mapObject", map, Map.class);
    TestMgr.check("{k=v}", result);
    TestMgr.check(LinkedHashMap.class, result.getClass());
  }

  private void testEmptyObject_highway() {
    // This is a behavior change in 2.0.0, before 2.0.0 this return null
    EmptyObject result = intf.testEmpty(new EmptyObject());
    TestMgr.check(result instanceof EmptyObject, true);

    result = restTemplate.postForObject(prefix + "/emptyObject", new EmptyObject(), EmptyObject.class);
    TestMgr.check(result instanceof EmptyObject, true);
  }

  private void testEmptyObject_rest() {
    EmptyObject result = intf.testEmpty(new EmptyObject());
    TestMgr.check(EmptyObject.class, result.getClass());

    result = restTemplate.postForObject(prefix + "/emptyObject", new EmptyObject(), EmptyObject.class);
    TestMgr.check(EmptyObject.class, result.getClass());
  }

  @SuppressWarnings("unchecked")
  private void testObject() {
    // int
    Object result = intf.testObject(1);
    TestMgr.check(1, result);
    TestMgr.check(Integer.class, result.getClass());

    result = restTemplate.postForObject(prefix + "/object", 1, Integer.class);
    TestMgr.check(1, result);
    TestMgr.check(Integer.class, result.getClass());

    // string
    result = intf.testObject("str");
    TestMgr.check("str", result);
    TestMgr.check(String.class, result.getClass());

    result = restTemplate.postForObject(prefix + "/object", "str", String.class);
    TestMgr.check("str", result);
    TestMgr.check(String.class, result.getClass());

    // emptyObject
    result = intf.testObject(new EmptyObject());
    // result may not be an empty map in highway
    //    TestMgr.check("{}", result);
    TestMgr.check(true, Map.class.isAssignableFrom(result.getClass()));

    result = restTemplate.postForObject(prefix + "/object", new EmptyObject(), EmptyObject.class);
    TestMgr.check(EmptyObject.class, result.getClass());
    result = restTemplate.postForObject(prefix + "/object", new EmptyObject(), EmptyObject.class);
    TestMgr.check(EmptyObject.class, result.getClass());

    // map
    Map<String, String> map = Collections.singletonMap("k", "v");
    result = intf.testObject(map);
    TestMgr.check("{k=v}", result);
    TestMgr.check(true, Map.class.isAssignableFrom(result.getClass()));

    result = restTemplate.postForObject(prefix + "/object", map, Map.class);
    TestMgr.check("{k=v}", result);
    TestMgr.check(true, Map.class.isAssignableFrom(result.getClass()));

    // list
    List<String> list = Collections.singletonList("v");
    result = intf.testObject(list);
    TestMgr.check("[v]", result);
    TestMgr.check(true, List.class.isAssignableFrom(result.getClass()));

    result = restTemplate.postForObject(prefix + "/object", list, List.class);
    TestMgr.check("[v]", result);
    TestMgr.check(true, List.class.isAssignableFrom(result.getClass()));

    // generic
    Holder<String> holder = new Holder<>("v");
    result = intf.testObject(holder);
    TestMgr.check("v", ((Map<String, String>) result).get("value"));
    TestMgr.check(LinkedHashMap.class, result.getClass());

    result = restTemplate.postForObject(prefix + "/object", holder, Holder.class);
    TestMgr.check("v", ((Map<String, String>) result).get("value"));
    TestMgr.check(LinkedHashMap.class, result.getClass());
  }
}
