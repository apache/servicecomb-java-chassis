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

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.common.rest.codec.RestObjectMapper;
import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.demo.CodeFirstRestTemplate;
import org.apache.servicecomb.demo.DemoConst;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.compute.Person;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class JaxrsClient {
  private static RestTemplate templateNew = RestTemplateBuilder.create();

  public static void main(String[] args) throws Exception {
    init();

    run();

    TestMgr.summary();
  }

  public static void init() throws Exception {
    Log4jUtils.init();
    BeanUtils.init();
  }

  public static void run() throws Exception {
    CodeFirstRestTemplate codeFirstClient = new CodeFirstRestTemplateJaxrs();
    codeFirstClient.testCodeFirst(templateNew, "jaxrs", "/codeFirstJaxrs/");
    testCompute(templateNew);
  }

  private static void testCompute(RestTemplate template) throws Exception {
    String microserviceName = "jaxrs";
    for (String transport : DemoConst.transports) {
      CseContext.getInstance().getConsumerProviderManager().setTransport(microserviceName, transport);
      TestMgr.setMsg(microserviceName, transport);

      String cseUrlPrefix = "cse://" + microserviceName;

      testGet(template, cseUrlPrefix);
      testPost(template, cseUrlPrefix);
      testPut(template, cseUrlPrefix);
      testDelete(template, cseUrlPrefix);
      testExchange(template, cseUrlPrefix);
      testRawJsonParam(template, cseUrlPrefix);
    }
  }

  private static void testGet(RestTemplate template, String cseUrlPrefix) {
    Map<String, String> params = new HashMap<>();
    params.put("a", "5");
    params.put("b", "3");
    int result =
        template.getForObject(cseUrlPrefix + "/compute/reduce?a={a}&b={b}", Integer.class, params);
    TestMgr.check(2, result);

    result = template.getForObject(cseUrlPrefix + "/compute/reduce?a={a}&b={b}", Integer.class, 5, 4);
    TestMgr.check(1, result);

    result = template.getForObject(cseUrlPrefix + "/compute/reduce?a=5&b=6",
        Integer.class);
    TestMgr.check(-1, result);
  }

  private static void testPost(RestTemplate template, String cseUrlPrefix) {
    Map<String, String> params = new HashMap<>();
    params.put("a", "5");
    params.put("b", "3");
    int result =
        template.postForObject(cseUrlPrefix + "/compute/add", params, Integer.class);
    TestMgr.check(8, result);

    Person person = new Person();
    person.setName("world");
    Person resultPerson =
        template.postForObject(cseUrlPrefix + "/compute/sayhello", person, Person.class);
    TestMgr.check("hello world", resultPerson.getName());

    HttpHeaders headers = new HttpHeaders();
    headers.add("prefix", "haha");
    HttpEntity<Person> reqEntity = new HttpEntity<>(person, headers);
    TestMgr.check("haha world",
        template.postForObject(cseUrlPrefix + "/compute/saysomething", reqEntity, String.class));
  }

  private static void testPut(RestTemplate template, String cseUrlPrefix) {
    template.put(cseUrlPrefix + "/compute/sayhi/{name}", null, "world");
  }

  private static void testDelete(RestTemplate template, String cseUrlPrefix) {
    Map<String, String> params = new HashMap<>();
    params.put("name", "world");
    template.delete(cseUrlPrefix + "/compute/sayhei/?name={name}", params);
  }

  private static void testExchange(RestTemplate template, String cseUrlPrefix) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", MediaType.APPLICATION_JSON);
    Person person = new Person();
    person.setName("world");
    HttpEntity<Person> requestEntity = new HttpEntity<>(person, headers);
    ResponseEntity<Person> resEntity = template.exchange(cseUrlPrefix + "/compute/sayhello",
        HttpMethod.POST,
        requestEntity,
        Person.class);
    TestMgr.check("hello world", resEntity.getBody());

    ResponseEntity<String> resEntity2 =
        template.exchange(cseUrlPrefix + "/compute/addstring?s=abc&s=def", HttpMethod.DELETE, null, String.class);
    TestMgr.check("abcdef", resEntity2.getBody());
  }

  private static void testRawJsonParam(RestTemplate template, String cseUrlPrefix) throws Exception {
    Map<String, String> person = new HashMap<>();
    person.put("name", "Tom");
    String jsonPerson = RestObjectMapper.INSTANCE.writeValueAsString(person);
    TestMgr.check("hello Tom",
        template.postForObject(cseUrlPrefix + "/compute/testrawjson", jsonPerson, String.class));
  }
}
