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

package org.apache.servicecomb.demo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.config.InMemoryDynamicPropertiesSource;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.demo.compute.Person;
import org.apache.servicecomb.demo.ignore.InputModelForTestIgnore;
import org.apache.servicecomb.demo.ignore.OutputModelForTestIgnore;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import io.vertx.core.json.JsonObject;

public class CodeFirstRestTemplate {
  protected Environment environment;

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  protected void changeTransport(String microserviceName, String transport) {
    InMemoryDynamicPropertiesSource.update("servicecomb.references.transport." + microserviceName, transport);
    TestMgr.setMsg(microserviceName, transport);
  }

  public void testCodeFirst(RestOperations template, String microserviceName, String basePath) {
    String cseUrlPrefix = "cse://" + microserviceName + basePath;
    changeTransport(microserviceName, "highway");
    testOnlyHighway(template, cseUrlPrefix);

    changeTransport(microserviceName, CoreConst.RESTFUL);
    testOnlyRest(microserviceName, template, cseUrlPrefix);

    for (String transport : DemoConst.transports) {
      changeTransport(microserviceName, transport);
      testAllTransport(microserviceName, template, cseUrlPrefix);
    }
  }

  protected void testAllTransport(String microserviceName, RestOperations template, String cseUrlPrefix) {
    testCodeFirstUserMap(template, cseUrlPrefix);
    testCodeFirstTextPlain(template, cseUrlPrefix);
    testCodeFirstBytes(template, cseUrlPrefix);
    testCseResponse(microserviceName, template, cseUrlPrefix);
    testCodeFirstAddDate(template, cseUrlPrefix);

    testCodeFirstAdd(template, cseUrlPrefix);
    testCodeFirstAddString(template, cseUrlPrefix);
    testCodeFirstIsTrue(template, cseUrlPrefix);
    testCodeFirstSayHi2(template, cseUrlPrefix);
    testCodeFirstSayHi(template, cseUrlPrefix);
    testCodeFirstSaySomething(template, cseUrlPrefix);
    testCodeFirstSayHello(template, cseUrlPrefix);
    testCodeFirstReduce(template, cseUrlPrefix);

    testTraceIdOnContextContainsTraceId(template, cseUrlPrefix);

    testRawJson(template, cseUrlPrefix);
  }

  protected void testOnlyHighway(RestOperations template, String cseUrlPrefix) {

  }

  protected void testOnlyRest(String microserviceName, RestOperations template, String cseUrlPrefix) {
    testCodeFirstUserMap(template, cseUrlPrefix);
    testCodeFirstTextPlain(template, cseUrlPrefix);
    testCodeFirstBytes(template, cseUrlPrefix);
    testCseResponse(microserviceName, template, cseUrlPrefix);
    testCodeFirstAddDate(template, cseUrlPrefix);
    testCodeFirstAdd(template, cseUrlPrefix);
    testCodeFirstAddString(template, cseUrlPrefix);

    testModelFieldIgnore(template, cseUrlPrefix);
    testCodeFirstReduce(template, cseUrlPrefix);
    // only rest transport will set trace id
    testTraceIdOnNotSetBefore(template, cseUrlPrefix);
  }

  private void testCodeFirstUserMap(RestOperations template, String cseUrlPrefix) {
    User user1 = new User();
    user1.setNames(new String[] {"u1", "u2"});

    User user2 = new User();
    user2.setNames(new String[] {"u3", "u4"});

    Map<String, User> userMap = new HashMap<>();
    userMap.put("u1", user1);
    userMap.put("u2", user2);

    // TODO: shall we support this usage? Seams not valid, cause result should be Map<String, Object> and type not defined.
//    @SuppressWarnings("unchecked")
//    Map<String, User> result = template.postForObject(cseUrlPrefix + "testUserMap",
//        userMap,
//        Map.class);

    Map<String, User> result = template.exchange(cseUrlPrefix + "testUserMap", HttpMethod.POST,
        new HttpEntity<>(userMap),
        new ParameterizedTypeReference<Map<String, User>>() {
        }).getBody();

    TestMgr.check("u1", result.get("u1").getNames()[0]);
    TestMgr.check("u2", result.get("u1").getNames()[1]);
    TestMgr.check("u3", result.get("u2").getNames()[0]);
    TestMgr.check("u4", result.get("u2").getNames()[1]);
  }

  private void testCodeFirstTextPlain(RestOperations template, String cseUrlPrefix) {
    String body = "a=1";
    String result = template.postForObject(cseUrlPrefix + "textPlain",
        body,
        String.class);
    TestMgr.check(body, result);
  }

  private void testCodeFirstBytes(RestOperations template, String cseUrlPrefix) {
    byte[] body = new byte[] {0, 1, 2};
    byte[] result = template.postForObject(cseUrlPrefix + "bytes",
        body,
        byte[].class);
    TestMgr.check(3, result.length);
    TestMgr.check(1, result[0]);
    TestMgr.check(1, result[1]);
    TestMgr.check(2, result[2]);
  }

  protected void checkStatusCode(String microserviceName, int expectStatusCode, HttpStatusCode httpStatus) {
    TestMgr.check(expectStatusCode, httpStatus.value());
  }

  private void testCseResponse(String targetMicroserviceName, RestOperations template,
      String cseUrlPrefix) {
    String srcMicroserviceName = BootStrapProperties.readServiceName(environment);

    ResponseEntity<User> responseEntity =
        template.exchange(cseUrlPrefix + "cseResponse", HttpMethod.GET, null, User.class);
    TestMgr.check("User [name=nameA, age=100, index=0]", responseEntity.getBody());
    TestMgr.check("h1v " + srcMicroserviceName, responseEntity.getHeaders().getFirst("h1"));
    TestMgr.check("h2v " + srcMicroserviceName, responseEntity.getHeaders().getFirst("h2"));
    checkStatusCode(targetMicroserviceName, 202, responseEntity.getStatusCode());
  }

  private void testCodeFirstAddDate(RestOperations template, String cseUrlPrefix) {
    Map<String, Object> body = new HashMap<>();
    Date date = new Date();
    body.put("date", date);

    int seconds = 1;
    Date result = template.postForObject(cseUrlPrefix + "addDate?seconds={seconds}",
        body,
        Date.class,
        seconds);
    TestMgr.check(new Date(date.getTime() + seconds * 1000), result);
  }

  private void testCodeFirstAddString(RestOperations template, String cseUrlPrefix) {
    ResponseEntity<String> responseEntity =
        template.exchange(cseUrlPrefix + "addstring?s=a&s=b",
            HttpMethod.DELETE,
            null,
            String.class);
    TestMgr.check("ab", responseEntity.getBody());
  }

  private void testCodeFirstIsTrue(RestOperations template, String cseUrlPrefix) {
    boolean result = template.getForObject(cseUrlPrefix + "istrue", boolean.class);
    TestMgr.check(true, result);
  }

  private void testCodeFirstSayHi2(RestOperations template, String cseUrlPrefix) {
    ResponseEntity<String> responseEntity =
        template.exchange(cseUrlPrefix + "sayhi/{name}/v2", HttpMethod.PUT, null, String.class, "world");
    TestMgr.check("world sayhi 2", responseEntity.getBody());
  }

  private void testCodeFirstSayHi(RestOperations template, String cseUrlPrefix) {
    ResponseEntity<String> responseEntity =
        template.exchange(cseUrlPrefix + "sayhi/{name}", HttpMethod.PUT, null, String.class, "world");
    TestMgr.check(202, responseEntity.getStatusCode().value());
    TestMgr.check("world sayhi", responseEntity.getBody());
  }

  private void testCodeFirstSaySomething(RestOperations template, String cseUrlPrefix) {
    Person person = new Person();
    person.setName("person name");

    HttpHeaders headers = new HttpHeaders();
    headers.add("prefix", "prefix  prefix");
    headers.add("userId", "serviceCombUser");

    HttpEntity<Person> requestEntity = new HttpEntity<>(person, headers);
    String result = template.postForObject(cseUrlPrefix + "saysomething", requestEntity, String.class);
    TestMgr.check("prefix  prefix person name", result);
  }

  private void testCodeFirstSayHello(RestOperations template, String cseUrlPrefix) {
    Map<String, String> persionFieldMap = new HashMap<>();
    persionFieldMap.put("name", "person name from map");
    Person result = template.postForObject(cseUrlPrefix + "sayhello", persionFieldMap, Person.class);
    TestMgr.check("hello person name from map", result);

    Person input = new Person();
    input.setName("person name from Object");
    result = template.postForObject(cseUrlPrefix + "sayhello", input, Person.class);
    TestMgr.check("hello person name from Object", result);
  }

  private void testCodeFirstAdd(RestOperations template, String cseUrlPrefix) {
    Map<String, String> params = new HashMap<>();
    params.put("a", "5");
    params.put("b", "3");
    int result =
        template.postForObject(cseUrlPrefix + "add", params, Integer.class);
    TestMgr.check(8, result);
  }

  private void testCodeFirstReduce(RestOperations template, String cseUrlPrefix) {
    Map<String, String> params = new HashMap<>();
    params.put("a", "5");

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, "b=3");

    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<Integer> result =
        template.exchange(cseUrlPrefix + "reduce?a={a}", HttpMethod.GET, requestEntity, Integer.class, params);
    TestMgr.check(2, result.getBody());
  }

  private void testModelFieldIgnore(RestOperations template, String cseUrlPrefix) {
    InputModelForTestIgnore input = new InputModelForTestIgnore("input_id_rest", "input_id_content",
        new Person("inputSomeone"), new JsonObject("{\"InputJsonKey\" : \"InputJsonValue\"}"), () -> {
    });
    OutputModelForTestIgnore output = template
        .postForObject(cseUrlPrefix + "ignore", input, OutputModelForTestIgnore.class);

    TestMgr.check(null, output.getInputId());
    TestMgr.check(input.getContent(), output.getContent());
    TestMgr.check(null, output.getOutputId());

    TestMgr.check(null, output.getInputIgnoreInterface());
    TestMgr.check(null, output.getInputJsonObject());
    TestMgr.check(null, output.getInputObject());

    TestMgr.check(null, output.getOutputIgnoreInterface());
    TestMgr.check(null, output.getOutputJsonObject());
    TestMgr.check(null, output.getOutputObject());
  }

  private void testRawJson(RestOperations template, String cseUrlPrefix) {
    String input = "{\"name\" : \"zyy\"}";
    String output = template.postForObject(cseUrlPrefix + "rawJsonAnnotation", input, String.class);
    TestMgr.check("hello zyy", output);
  }

  private void testTraceIdOnNotSetBefore(RestOperations template, String cseUrlPrefix) {
    String traceIdUrl = cseUrlPrefix + "traceId";
    String result = template.getForObject(traceIdUrl, String.class);
    TestMgr.checkNotEmpty(result);
  }

  private void testTraceIdOnContextContainsTraceId(RestOperations template, String cseUrlPrefix) {
    String traceIdUrl = cseUrlPrefix + "traceId";
    InvocationContext invocationContext = new InvocationContext();
    invocationContext.addContext(CoreConst.TRACE_ID_NAME, String.valueOf(Long.MIN_VALUE));
    ContextUtils.setInvocationContext(invocationContext);
    String result = template.getForObject(traceIdUrl, String.class);
    TestMgr.check(String.valueOf(Long.MIN_VALUE), result);
    ContextUtils.removeInvocationContext();
  }
}
