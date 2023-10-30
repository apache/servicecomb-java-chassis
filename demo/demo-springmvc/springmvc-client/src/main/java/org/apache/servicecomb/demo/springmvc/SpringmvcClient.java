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

package org.apache.servicecomb.demo.springmvc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.servicecomb.config.InMemoryDynamicPropertiesSource;
import org.apache.servicecomb.demo.CategorizedTestCaseRunner;
import org.apache.servicecomb.demo.DemoConst;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.controller.Controller;
import org.apache.servicecomb.demo.controller.Person;
import org.apache.servicecomb.demo.springmvc.client.CodeFirstRestTemplateSpringmvc;
import org.apache.servicecomb.demo.springmvc.client.ThirdSvc.ThirdSvcClient;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClients;
import org.apache.servicecomb.provider.springmvc.reference.CseRestTemplate;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.provider.springmvc.reference.UrlWithProviderPrefixClientHttpRequestFactory;
import org.apache.servicecomb.provider.springmvc.reference.UrlWithServiceNameClientHttpRequestFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
@ImportResource(value = "classpath*:META-INF/spring/*.bean.xml")
public class SpringmvcClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(SpringmvcClient.class);

  private static RestTemplate templateUrlWithServiceName = new CseRestTemplate();

  private static RestTemplate templateUrlWithProviderPrefix = new CseRestTemplate();

  private static RestOperations restTemplate;

  private static Controller controller;

  public static void main(String[] args) throws Exception {
    try {
      new SpringApplicationBuilder(SpringmvcClient.class).web(WebApplicationType.NONE).run(args);

      run();
    } catch (Throwable e) {
      TestMgr.check("success", "failed");
      LOGGER.error("-------------- test failed -------------");
      LOGGER.error("", e);
      LOGGER.error("-------------- test failed -------------");
    }
    TestMgr.summary();
    LOGGER.info("-------------- last time updated checks(maybe more/less): 1313 -------------");
  }

  private static void changeTransport(String microserviceName, String transport) {
    InMemoryDynamicPropertiesSource.update("servicecomb.references.transport." + microserviceName, transport);
    TestMgr.setMsg(microserviceName, transport);
  }

  public static void run() throws Exception {
    testHttpClientsIsOk();

    templateUrlWithServiceName.setRequestFactory(new UrlWithServiceNameClientHttpRequestFactory());
    restTemplate = RestTemplateBuilder.create();
    templateUrlWithProviderPrefix.setRequestFactory(new UrlWithProviderPrefixClientHttpRequestFactory("/pojo/rest"));
    controller = BeanUtils.getBean("controller");

    String prefix = "cse://springmvc";
    String microserviceName = "springmvc";

    try {
      // this test class is intended for retry hanging issue JAV-127
      templateUrlWithServiceName.getForObject(prefix + "/controller/sayhi?name=throwexception", String.class);
      TestMgr.check("true", "false");
    } catch (Exception e) {
      TestMgr.check("true", "true");
    }
    testHandler(microserviceName);
    CodeFirstRestTemplateSpringmvc codeFirstClient =
        BeanUtils.getContext().getBean(CodeFirstRestTemplateSpringmvc.class);
    codeFirstClient.testCodeFirst(restTemplate, "springmvc", "/codeFirstSpringmvc/");
    codeFirstClient.testCodeFirst(templateUrlWithProviderPrefix, "springmvc", "/pojo/rest/codeFirstSpringmvc/");
    testAllTransport(microserviceName);
    testRestTransport(microserviceName, prefix);
    CategorizedTestCaseRunner.runCategorizedTestCase(microserviceName);
  }

  private static void testHandler(String microserviceName) {
    changeTransport(microserviceName, "rest");
    String prefix = "cse://springmvc";
    String result = templateUrlWithServiceName.getForObject(prefix + "/controller/sayHello1?name=tom", String.class);
    TestMgr.check("Hello tom,v", result);
  }

  private static void testHttpClientsIsOk() {
    TestMgr.check(HttpClients.getClient("http-transport-client") != null, true);
    TestMgr.check(HttpClients.getClient("http2-transport-client") != null, true);

    TestMgr.check(HttpClients.getClient("http-transport-client", false) != null, true);
    TestMgr.check(HttpClients.getClient("http2-transport-client", false) != null, true);
  }

  private static void testRestTransport(String microserviceName, String prefix) {
    changeTransport(microserviceName, "rest");

    testControllerRest(templateUrlWithServiceName, microserviceName);
    testSpringMvcDefaultValuesRest(templateUrlWithServiceName, microserviceName);
    testSpringMvcDefaultValuesJavaPrimitiveRest(templateUrlWithServiceName, microserviceName);

    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept-Encoding", "gzip");
    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<String> entityCompress =
        restTemplate.exchange(prefix
            + "/codeFirstSpringmvc/sayhi/compressed/{name}/v2", HttpMethod.GET, entity, String.class, "Test");
    TestMgr.check(
        "Test sayhi compressed:This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text,This is a big text!",
        entityCompress.getBody());
    // if server response is compressed, the content-length header will be removed , so can't check this.
    // the transfer-encoding header will be missing when the server is set to not compressed
    if (entityCompress.getHeaders().get("transfer-encoding") != null) {
      TestMgr.check("chunked", entityCompress.getHeaders().get("transfer-encoding").get(0));
    }

    //0.5.0 later version metrics integration test
    @SuppressWarnings("unchecked")
    Map<String, Double> metrics = restTemplate.getForObject(prefix + "/scb/metrics", Map.class);

    //    TestMgr.check(true, metrics.get("jvm(name=heapUsed,statistic=gauge)") != 0);
    TestMgr.check(true, metrics.size() > 0);

    //prometheus integration test
    try {
      String content = restTemplate
          .getForObject("cse://springmvc/codeFirstSpringmvc/prometheusForTest", String.class);

      String application = LegacyPropertyFactory.getStringProperty("servicecomb.service.application", "");

      TestMgr.check(true,
          content.contains(
              "servicecomb_invocation{appId=\"" + application + "\",operation=\"springmvc.codeFirst.addDate"));
      TestMgr.check(true,
          content.contains(
              "servicecomb_invocation{appId=\"" + application + "\",operation=\"springmvc.codeFirst.sayHello"));
      TestMgr.check(true,
          content
              .contains("servicecomb_invocation{appId=\"" + application + "\",operation=\"springmvc.codeFirst.isTrue"));
      TestMgr.check(true,
          content.contains("servicecomb_invocation{appId=\"" + application + "\",operation=\"springmvc.codeFirst.add"));
      TestMgr.check(true,
          content
              .contains("servicecomb_invocation{appId=\"" + application + "\",operation=\"springmvc.codeFirst.sayHi2"));
      TestMgr.check(true, content
          .contains(
              "servicecomb_invocation{appId=\"" + application + "\",operation=\"springmvc.codeFirst.saySomething"));

      String[] metricLines = content.split("\n");
      if (metricLines.length > 0) {
        for (String metricLine : metricLines) {
          if (!metricLine.startsWith("#")) {
            String[] metricKeyAndValue = metricLine.split(" ");
            if (!metricKeyAndValue[0].startsWith("jvm") && !metricKeyAndValue[0].startsWith("os")) {
              if (Double.parseDouble(metricKeyAndValue[1]) < 0) {
                TestMgr.check("true", "false");
                break;
              }
            }
          }
        }
      } else {
        TestMgr.check("true", "false");
      }
    } catch (Exception e) {
      LOGGER.error("", e);
      TestMgr.check("true", "false");
    }
  }

  private static void testAllTransport(String microserviceName) {
    for (String transport : DemoConst.transports) {
      changeTransport(microserviceName, transport);

      TestMgr.setMsg(microserviceName, transport);

      testControllerAllTransport(templateUrlWithServiceName, microserviceName);

      testController();
      testRequiredBody(templateUrlWithServiceName, microserviceName);
      testSpringMvcDefaultValuesAllTransport(templateUrlWithServiceName, microserviceName);
      testSpringMvcDefaultValuesJavaPrimitiveAllTransport(templateUrlWithServiceName, microserviceName);
      testThirdService();
    }
  }

  private static void testThirdService() {
    ThirdSvcClient client = BeanUtils.getContext().getBean(ThirdSvcClient.class);

    Date date = new Date();
    ResponseEntity<Date> responseEntity = client.responseEntity(date);
    TestMgr.check(date, responseEntity.getBody());
  }

  private static void testControllerRest(RestTemplate template, String microserviceName) {
    String prefix = "cse://" + microserviceName;

    TestMgr.check("hi world [world]",
        template.getForObject(prefix + "/controller/sayhi?name=world",
            String.class));

    TestMgr.check("hi world boot [world boot]",
        template.getForObject(prefix + "/controller/sayhi?name=world boot",
            String.class));

    TestMgr.check("hi world boot [world boot]",
        template.getForObject(prefix + "/controller/sayhi?name=world+boot",
            String.class));

    TestMgr.check("hi world1 [world1]",
        template.getForObject(prefix + "/controller/sayhi?name={name}",
            String.class,
            "world1"));

    TestMgr.check("hi world1+world2 [world1+world2]",
        template.getForObject(prefix + "/controller/sayhi?name={name}",
            String.class,
            "world1+world2"));

    TestMgr.check("hi hi 中国 [hi 中国]",
        template.getForObject(prefix + "/controller/sayhi?name={name}",
            String.class,
            "hi 中国"));

    Map<String, String> params = new HashMap<>();
    params.put("name", "world2");
    TestMgr.check("hi world2 [world2]",
        template.getForObject(prefix + "/controller/sayhi?name={name}",
            String.class,
            params));

    try {
      template.postForObject(prefix + "/controller/sayhello/{name}",
          null,
          String.class,
          "exception");
      TestMgr.check(true, false);
    } catch (InvocationException e) {
      TestMgr.check(e.getStatusCode(), 503);
    }
  }

  private static void testControllerAllTransport(RestTemplate template, String microserviceName) {
    String prefix = "cse://" + microserviceName;

    TestMgr.check(7,
        template.getForObject(prefix + "/controller/add?a=3&b=4",
            Integer.class));

    try {
      template.getForObject(prefix + "/controller/add",
          Integer.class);
      TestMgr.check("failed", "success");
    } catch (InvocationException e) {
      TestMgr.check(e.getStatusCode(), 400);
    }

    TestMgr.check("hello world",
        template.postForObject(prefix + "/controller/sayhello/{name}",
            null,
            String.class,
            "world"));
    TestMgr.check("hello hello 中国",
        template.postForObject(prefix + "/controller/sayhello/{name}",
            null,
            String.class,
            "hello 中国"));

    try {
      template.postForObject(prefix + "/controller/sayhello/{name}",
          null,
          String.class,
          "exception");
      TestMgr.check(true, false);
    } catch (InvocationException e) {
      TestMgr.check(e.getStatusCode(), 503);
    }

    HttpHeaders headers = new HttpHeaders();
    headers.add("name", "world");
    @SuppressWarnings("rawtypes")
    HttpEntity entity = new HttpEntity<>(null, headers);
    ResponseEntity<String> response = template.exchange(prefix + "/controller/sayhei",
        HttpMethod.GET,
        entity,
        String.class);
    TestMgr.check("hei world", response.getBody());

    Person user = new Person();
    user.setName("world");
    TestMgr.check("ha world",
        template.postForObject(prefix + "/controller/saysomething?prefix={prefix}",
            user,
            String.class,
            "ha"));
  }

  private static void testController() {
    Person user = new Person();
    user.setName("world");
    TestMgr.check("ha world", controller.saySomething("ha", user));
  }

  private static void testRequiredBody(RestTemplate template, String microserviceName) {
    String prefix = "cse://" + microserviceName;
    Person user = new Person();

    TestMgr.check("No user data found",
        template.postForObject(prefix + "/annotations/saysomething?prefix={prefix}",
            user,
            String.class,
            "ha"));

    user.setName("world");
    TestMgr.check("ha world",
        template.postForObject(prefix + "/annotations/saysomething?prefix={prefix}",
            user,
            String.class,
            "ha"));

    TestMgr.check("No user data found",
        template.postForObject(prefix + "/annotations/saysomething?prefix={prefix}",
            null,
            String.class,
            "ha"));

    TestMgr.check("No user name found",
        template.postForObject(prefix + "/annotations/say",
            "",
            String.class,
            "ha"));
    TestMgr.check("test",
        template.postForObject(prefix + "/annotations/say",
            "test",
            String.class,
            "ha"));

    try {
      template.postForObject(prefix + "/annotations/testRequiredBody",
          null,
          String.class);
      TestMgr.fail("should fail");
    } catch (InvocationException e) {
      TestMgr.check(e.getStatusCode(), HttpStatus.SC_BAD_REQUEST);
    }
  }

  private static void testSpringMvcDefaultValuesRest(RestTemplate template, String microserviceName) {
    String cseUrlPrefix = "cse://" + microserviceName + "/SpringMvcDefaultValues/";
    String result = template.getForObject(cseUrlPrefix + "/query?d=10", String.class);
    TestMgr.check("Hello 20bobo4010", result);
    boolean failed = false;
    result = null;
    try {
      result = template.getForObject(cseUrlPrefix + "/query2", String.class);
    } catch (InvocationException e) {
      failed = true;
      TestMgr.check(e.getStatusCode(), HttpStatus.SC_BAD_REQUEST);
    }
    TestMgr.check(true, failed);
    TestMgr.check(null, result);

    failed = false;
    result = null;
    try {
      result = template.getForObject(cseUrlPrefix + "/query2?d=2&e=2", String.class);
    } catch (InvocationException e) {
      failed = true;
      TestMgr.check(e.getStatusCode(), HttpStatus.SC_BAD_REQUEST);
    }
    TestMgr.check(true, failed);
    TestMgr.check(null, result);

    failed = false;
    result = null;
    try {
      result = template.getForObject(cseUrlPrefix + "/query2?a=&d=2&e=2", String.class);
    } catch (InvocationException e) {
      failed = true;
      TestMgr.check(e.getStatusCode(), HttpStatus.SC_BAD_REQUEST);
    }
    TestMgr.check(true, failed);
    TestMgr.check(null, result);

    result = template.getForObject(cseUrlPrefix + "/query2?d=30&e=2", String.class);
    TestMgr.check("Hello 20bobo40302", result);

    failed = false;
    result = null;
    try {
      result = template.getForObject(cseUrlPrefix + "/query3?a=2&b=2", String.class);
    } catch (InvocationException e) {
      failed = true;
      TestMgr.check(e.getStatusCode(), HttpStatus.SC_BAD_REQUEST);
    }
    TestMgr.check(true, failed);
    TestMgr.check(null, result);
  }

  private static void testSpringMvcDefaultValuesAllTransport(RestTemplate template, String microserviceName) {
    String cseUrlPrefix = "cse://" + microserviceName + "/SpringMvcDefaultValues/";
    //default values
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
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
    result = null;
    try {
      result = template.getForObject(cseUrlPrefix + "/query2", String.class);
    } catch (InvocationException e) {
      failed = true;
      TestMgr.check(e.getStatusCode(), HttpStatus.SC_BAD_REQUEST);
    }
    TestMgr.check(true, failed);
    TestMgr.check(null, result);

    failed = false;
    result = null;
    try {
      result = template.getForObject(cseUrlPrefix + "/query2?d=2&e=2", String.class);
    } catch (InvocationException e) {
      failed = true;
      TestMgr.check(e.getStatusCode(), HttpStatus.SC_BAD_REQUEST);
    }
    TestMgr.check(true, failed);
    TestMgr.check(null, result);

    failed = false;
    result = null;
    try {
      result = template.getForObject(cseUrlPrefix + "/query2?a=&d=2&e=2", String.class);
    } catch (InvocationException e) {
      failed = true;
      TestMgr.check(e.getStatusCode(), HttpStatus.SC_BAD_REQUEST);
    }
    TestMgr.check(true, failed);
    TestMgr.check(null, result);

    result = template.getForObject(cseUrlPrefix + "/query2?d=30&e=2", String.class);
    TestMgr.check("Hello 20bobo40302", result);

    failed = false;
    result = null;
    try {
      result = template.getForObject(cseUrlPrefix + "/query3?a=2&b=2", String.class);
    } catch (InvocationException e) {
      failed = true;
      TestMgr.check(e.getStatusCode(), HttpStatus.SC_BAD_REQUEST);
    }
    TestMgr.check(true, failed);
    TestMgr.check(null, result);

    result = template.getForObject(cseUrlPrefix + "/query3?a=30&b=2", String.class);
    TestMgr.check("Hello 302", result);

    result = template.getForObject(cseUrlPrefix + "/query3?a=30", String.class);
    TestMgr.check("Hello 30null", result);

    //input values
    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    HttpEntity<Map<String, String>> requestPara = new HttpEntity<>(null, headers);
    result = template.postForObject(cseUrlPrefix + "/form?a=30&b=sam", requestPara, String.class);
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

  private static void testSpringMvcDefaultValuesJavaPrimitiveAllTransport(RestTemplate template,
      String microserviceName) {
    String cseUrlPrefix = "cse://" + microserviceName + "/SpringMvcDefaultValues/";
    //default values with primitive
    String result = template.postForObject(cseUrlPrefix + "/javaprimitiveint", null, String.class);
    TestMgr.check("Hello 0bobo", result);

    result = template.postForObject(cseUrlPrefix + "/javaprimitivenumber", null, String.class);
    TestMgr.check("Hello 0.0false", result);

    result = template.postForObject(cseUrlPrefix + "/javaprimitivestr", null, String.class);
    TestMgr.check("Hello", result);

    result = template.postForObject(cseUrlPrefix + "/javaprimitivecomb", null, String.class);
    TestMgr.check("Hello nullnull", result);

    result = template.postForObject(cseUrlPrefix + "/allprimitivetypes", null, String.class);
    TestMgr.check("Hello false,\0,0,0,0,0,0.0,0.0,null", result);

    result = template.postForObject(cseUrlPrefix
            + "/allprimitivetypes?pBoolean=true&pChar=c&pByte=20&pShort=30&pInt=40&pLong=50&pFloat=60&pDouble=70&pDoubleWrap=80",
        null, String.class);
    TestMgr.check("Hello true,c,20,30,40,50,60.0,70.0,80.0", result);
  }

  private static void testSpringMvcDefaultValuesJavaPrimitiveRest(RestTemplate template, String microserviceName) {
    String cseUrlPrefix = "cse://" + microserviceName + "/SpringMvcDefaultValues/";
    //default values with primitive
    String result = template.postForObject(cseUrlPrefix + "/javaprimitiveint", null, String.class);
    TestMgr.check("Hello 0bobo", result);

    result = template.postForObject(cseUrlPrefix + "/javaprimitivenumber", null, String.class);
    TestMgr.check("Hello 0.0false", result);

    result = template.postForObject(cseUrlPrefix + "/javaprimitivestr", null, String.class);
    TestMgr.check("Hello", result);

    result = template.postForObject(cseUrlPrefix + "/javaprimitivecomb", null, String.class);
    TestMgr.check("Hello nullnull", result);

    result = template.postForObject(cseUrlPrefix + "/allprimitivetypes", null, String.class);
    TestMgr.check("Hello false,\0,0,0,0,0,0.0,0.0,null", result);
  }
}
