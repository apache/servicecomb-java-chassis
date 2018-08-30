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
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpStatus;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.demo.CodeFirstRestTemplate;
import org.apache.servicecomb.demo.DemoConst;
import org.apache.servicecomb.demo.RestObjectMapperWithStringMapper;
import org.apache.servicecomb.demo.RestObjectMapperWithStringMapperNotWriteNull;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.compute.Person;
import org.apache.servicecomb.demo.jaxrs.client.beanParam.BeanParamPojoClient;
import org.apache.servicecomb.demo.jaxrs.client.beanParam.BeanParamRestTemplateClient;
import org.apache.servicecomb.demo.jaxrs.client.pojoDefault.DefaultModelServiceClient;
import org.apache.servicecomb.demo.jaxrs.client.validation.ValidationServiceClient;
import org.apache.servicecomb.demo.validator.Student;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
    RestObjectMapperFactory.setDefaultRestObjectMapper(new RestObjectMapperWithStringMapper());
    RestObjectMapperFactory.setConsumerWriterMapper(new RestObjectMapperWithStringMapperNotWriteNull());
  }

  public static void run() throws Exception {
    CodeFirstRestTemplate codeFirstClient = new CodeFirstRestTemplateJaxrs();
    codeFirstClient.testCodeFirst(templateNew, "jaxrs", "/codeFirstJaxrs/");
    testCompute(templateNew);
    testValidator(templateNew);
    testClientTimeOut(templateNew);
    testJaxRSDefaultValues(templateNew);
    testSpringMvcDefaultValuesJavaPrimitive(templateNew);
    MultiErrorCodeServiceClient.runTest();

    BeanParamPojoClient beanParamPojoClient = new BeanParamPojoClient();
    beanParamPojoClient.testAll();
    BeanParamRestTemplateClient beanParamRestTemplateClient = new BeanParamRestTemplateClient();
    beanParamRestTemplateClient.testAll();
    DefaultModelServiceClient.run();
    ValidationServiceClient.run();
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

  private static void testValidator(RestTemplate template) {
    String microserviceName = "jaxrs";
    for (String transport : DemoConst.transports) {
      CseContext.getInstance().getConsumerProviderManager().setTransport(microserviceName, transport);
      TestMgr.setMsg(microserviceName, transport);

      String cseUrlPrefix = "cse://" + microserviceName + "/validator/";

      testValidatorAddSuccess(template, cseUrlPrefix);
      testValidatorAddFail(template, cseUrlPrefix);
      testValidatorSayHiSuccess(template, cseUrlPrefix);
      testValidatorSayHiFail(template, cseUrlPrefix);
      testValidatorExchangeSuccess(template, cseUrlPrefix);
      testValidatorExchangeFail(template, cseUrlPrefix);
    }
  }


  private static void testJaxRSDefaultValues(RestTemplate template) {
    String microserviceName = "jaxrs";
    for (String transport : DemoConst.transports) {
      CseContext.getInstance().getConsumerProviderManager().setTransport(microserviceName, transport);
      TestMgr.setMsg(microserviceName, transport);

      String cseUrlPrefix = "cse://" + microserviceName + "/JaxRSDefaultValues/";

      //default values
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
      MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
      HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
      String result = template.postForObject(cseUrlPrefix + "/form", request, String.class);
      TestMgr.check("Hello 20bobo", result);

      headers = new HttpHeaders();
      HttpEntity<String> entity = new HttpEntity<>(null, headers);
      result = template.postForObject(cseUrlPrefix + "/header", entity, String.class);
      TestMgr.check("Hello 20bobo30", result);

      result = template.getForObject(cseUrlPrefix + "/query?d=10", String.class);
      TestMgr.check("Hello 20bobo4010", result);
      boolean failed = false;
      try {
        result = template.getForObject(cseUrlPrefix + "/query2", String.class);
      } catch (InvocationException e) {
        failed = true;
        TestMgr.check(e.getStatusCode(), HttpStatus.SC_BAD_REQUEST);
      }

      failed = false;
      try {
        result = template.getForObject(cseUrlPrefix + "/query2?d=2&e=2", String.class);
      } catch (InvocationException e) {
        failed = true;
        TestMgr.check(e.getStatusCode(), HttpStatus.SC_BAD_REQUEST);
      }
      TestMgr.check(failed, true);

      failed = false;
      try {
        result = template.getForObject(cseUrlPrefix + "/query2?a=&d=2&e=2", String.class);
      } catch (InvocationException e) {
        failed = true;
        TestMgr.check(e.getStatusCode(), HttpStatus.SC_BAD_REQUEST);
      }
      TestMgr.check(failed, true);

      result = template.getForObject(cseUrlPrefix + "/query2?d=30&e=2", String.class);
      TestMgr.check("Hello 20bobo40302", result);

      failed = false;
      try {
        result = template.getForObject(cseUrlPrefix + "/query3?a=2&b=2", String.class);
      } catch (InvocationException e) {
        failed = true;
        TestMgr.check(e.getStatusCode(), HttpStatus.SC_BAD_REQUEST);
      }
      TestMgr.check(failed, true);

      result = template.getForObject(cseUrlPrefix + "/query3?a=30&b=2", String.class);
      TestMgr.check("Hello 302", result);

      result = template.getForObject(cseUrlPrefix + "/query3?a=30", String.class);
      TestMgr.check("Hello 30null", result);

      //input values
      headers = new HttpHeaders();
      headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
      Map<String, String> params = new HashMap<>();
      params.put("a", "30");
      params.put("b", "sam");
      HttpEntity<Map<String, String>> requestPara = new HttpEntity<>(params, headers);
      result = template.postForObject(cseUrlPrefix + "/form", requestPara, String.class);
      TestMgr.check("Hello 30sam", result);

      headers = new HttpHeaders();
      headers.add("a", "30");
      headers.add("b", "sam");
      headers.add("c", "40");
      entity = new HttpEntity<>(null, headers);
      result = template.postForObject(cseUrlPrefix + "/header", entity, String.class);
      TestMgr.check("Hello 30sam40", result);

      result = template.getForObject(cseUrlPrefix + "/query?a=3&b=sam&c=5&d=30", String.class);
      TestMgr.check("Hello 3sam530", result);

      result = template.getForObject(cseUrlPrefix + "/query2?a=3&b=4&c=5&d=30&e=2", String.class);
      TestMgr.check("Hello 345302", result);
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
    String jsonPerson = RestObjectMapperFactory.getRestObjectMapper().writeValueAsString(person);
    TestMgr.check("hello Tom",
        template.postForObject(cseUrlPrefix + "/compute/testrawjson", jsonPerson, String.class));
  }

  @SuppressWarnings({"rawtypes"})
  private static void testValidatorAddFail(RestTemplate template, String cseUrlPrefix) {
    Map<String, String> params = new HashMap<>();
    params.put("a", "5");
    params.put("b", "3");
    boolean isExcep = false;
    try {
      template.postForObject(cseUrlPrefix + "add", params, Integer.class);
    } catch (InvocationException e) {
      isExcep = true;
      TestMgr.check(400, e.getStatus().getStatusCode());
      TestMgr.check(Status.BAD_REQUEST, e.getReasonPhrase());
      // Message dependends on locale, so just check the short part.
      // 'must be greater than or equal to 20', propertyPath=add.arg1, rootBeanClass=class org.apache.servicecomb.demo.jaxrs.server.Validator, messageTemplate='{javax.validation.constraints.Min.message}'}]]
      // ignored
      Map data = (Map) e.getErrorData();
      TestMgr.check(
          true, data.get("message").toString().contains("propertyPath=add.b"));
    }

    TestMgr.check(true, isExcep);
  }

  private static void testValidatorAddSuccess(RestTemplate template, String cseUrlPrefix) {
    Map<String, String> params = new HashMap<>();
    params.put("a", "5");
    params.put("b", "20");
    int result = template.postForObject(cseUrlPrefix + "add", params, Integer.class);
    TestMgr.check(25, result);
  }

  @SuppressWarnings({"rawtypes"})
  private static void testValidatorSayHiFail(RestTemplate template, String cseUrlPrefix) {
    boolean isExcep = false;
    try {
      template.exchange(cseUrlPrefix + "sayhi/{name}", HttpMethod.PUT, null, String.class, "te");
    } catch (InvocationException e) {
      isExcep = true;
      TestMgr.check(400, e.getStatus().getStatusCode());
      TestMgr.check(Status.BAD_REQUEST, e.getReasonPhrase());
      // Message dependends on locale, so just check the short part.
      Map data = (Map) e.getErrorData();
      TestMgr.check(
          true, data.get("message").toString().contains("propertyPath=sayHi.name"));
    }
    TestMgr.check(true, isExcep);
  }

  private static void testValidatorSayHiSuccess(RestTemplate template, String cseUrlPrefix) {
    ResponseEntity<String> responseEntity =
        template.exchange(cseUrlPrefix + "sayhi/{name}", HttpMethod.PUT, null, String.class, "world");
    TestMgr.check(202, responseEntity.getStatusCode());
    TestMgr.check("world sayhi", responseEntity.getBody());
  }

  @SuppressWarnings({"rawtypes"})
  private static void testValidatorExchangeFail(RestTemplate template, String cseUrlPrefix) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", MediaType.APPLICATION_JSON);
    Student student = new Student();
    student.setName("");
    student.setAge(25);
    boolean isExcep = false;
    try {
      HttpEntity<Student> requestEntity = new HttpEntity<>(student, headers);
      template.exchange(cseUrlPrefix + "/sayhello",
          HttpMethod.POST,
          requestEntity,
          Student.class);
    } catch (InvocationException e) {
      isExcep = true;
      TestMgr.check(400, e.getStatus().getStatusCode());
      TestMgr.check(Status.BAD_REQUEST, e.getReasonPhrase());
      // Message dependends on locale, so just check the short part.
      Map data = (Map) e.getErrorData();
      TestMgr.check(
          true, data.get("message").toString().contains("propertyPath=sayHello.student.age"));
    }
    TestMgr.check(true, isExcep);
  }

  private static void testValidatorExchangeSuccess(RestTemplate template, String cseUrlPrefix) {
    Student student = new Student();
    student.setName("test");
    student.setAge(15);
    Student result = template.postForObject(cseUrlPrefix + "sayhello", student, Student.class);
    TestMgr.check("hello test 15", result);
  }

  private static void testClientTimeOut(RestTemplate template) {
    String microserviceName = "jaxrs";
    for (String transport : DemoConst.transports) {
      if (transport.equals(Const.ANY_TRANSPORT)) {
        continue;
      }
      CseContext.getInstance().getConsumerProviderManager().setTransport(microserviceName, transport);
      TestMgr.setMsg(microserviceName, transport);

      String cseUrlPrefix = "cse://" + microserviceName + "/clientreqtimeout/";

      testClientTimeoutSayHi(template, cseUrlPrefix);
      testClientTimeoutAdd(template, cseUrlPrefix);
    }
  }

  private static void testClientTimeoutSayHi(RestTemplate template, String cseUrlPrefix) {
    Student student = new Student();
    student.setName("timeout");
    student.setAge(30);
    Student result = template.postForObject(cseUrlPrefix + "sayhello", student, Student.class);
    TestMgr.check("hello timeout 30", result);
  }

  private static void testClientTimeoutAdd(RestTemplate template, String cseUrlPrefix) {
    Map<String, String> params = new HashMap<>();
    params.put("a", "5");
    params.put("b", "20");
    boolean isExcep = false;
    try {
      template.postForObject(cseUrlPrefix + "add", params, Integer.class);
    } catch (InvocationException e) {
      isExcep = true;
      TestMgr.check(490, e.getStatus().getStatusCode());
      TestMgr.check(
          "CommonExceptionData [message=Cse Internal Bad Request]",
          e.getErrorData());
    }

    TestMgr.check(true, isExcep);
  }

  private static void testSpringMvcDefaultValuesJavaPrimitive(RestTemplate template) {
    String microserviceName = "jaxrs";
    String cseUrlPrefix = "cse://" + microserviceName + "/JaxRSDefaultValues/";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

    //default values with primitive
    String result = template.postForObject(cseUrlPrefix + "/javaprimitiveint", request, String.class);
    TestMgr.check("Hello 0bobo", result);

    result = template.postForObject(cseUrlPrefix + "/javaprimitivenumber", request, String.class);
    TestMgr.check("Hello 0.0false", result);

    result = template.postForObject(cseUrlPrefix + "/javaprimitivestr", request, String.class);
    TestMgr.check("Hello", result);

    result = template.postForObject(cseUrlPrefix + "/javaprimitivecomb", request, String.class);
    TestMgr.check("Hello nullnull", result);

    result = template.postForObject(cseUrlPrefix + "/allprimitivetypes", null, String.class);
    TestMgr.check("Hello false,0,0,0,0,0,0.0,0.0,null", result);
  }
}
