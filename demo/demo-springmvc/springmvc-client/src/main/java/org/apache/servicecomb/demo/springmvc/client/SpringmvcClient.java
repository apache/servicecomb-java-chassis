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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.demo.DemoConst;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.controller.Controller;
import org.apache.servicecomb.demo.controller.Person;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.apache.servicecomb.provider.springmvc.reference.CseRestTemplate;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.provider.springmvc.reference.UrlWithServiceNameClientHttpRequestFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SpringmvcClient {
  private static RestTemplate templateUrlWithServiceName = new CseRestTemplate();

  private static RestTemplate restTemplate;

  private static Controller controller;

  public static void main(String[] args) throws Exception {
    Log4jUtils.init();
    BeanUtils.init();

    run();

    TestMgr.summary();
  }

  public static void run() {
    templateUrlWithServiceName.setRequestFactory(new UrlWithServiceNameClientHttpRequestFactory());
    restTemplate = RestTemplateBuilder.create();
    controller = BeanUtils.getBean("controller");

    String prefix = "cse://springmvc";

    try {
      // this test class is intended for retry hanging issue JAV-127
      templateUrlWithServiceName.getForObject(prefix + "/controller/sayhi?name=throwexception", String.class);
      TestMgr.check("true", "false");
    } catch (Exception e) {
      TestMgr.check("true", "true");
    }

    CodeFirstRestTemplateSpringmvc codeFirstClient =
        BeanUtils.getContext().getBean(CodeFirstRestTemplateSpringmvc.class);
    codeFirstClient.testCodeFirst(restTemplate, "springmvc", "/codeFirstSpringmvc/");

    String microserviceName = "springmvc";
    for (String transport : DemoConst.transports) {
      CseContext.getInstance().getConsumerProviderManager().setTransport(microserviceName, transport);
      TestMgr.setMsg(microserviceName, transport);

      testController(templateUrlWithServiceName, microserviceName);

      testController();
    }
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
    Map<String, Double> metrics = restTemplate.getForObject(prefix + "/metrics", Map.class);

//    TestMgr.check(true, metrics.get("jvm(name=heapUsed,statistic=gauge)") != 0);
    TestMgr.check(true, metrics.size() > 0);
    TestMgr.check(true,
        metrics.get(
            "servicecomb.invocation(operation=springmvc.codeFirst.saySomething,role=PRODUCER,stage=total,statistic=count,status=200,transport=highway)") >= 0);

    //prometheus integration test
    try {
      String content = restTemplate.getForObject("cse://springmvc/codeFirstSpringmvc/prometheusForTest", String.class);

      TestMgr.check(true, content.contains("servicecomb_invocation{operation=\"springmvc.codeFirst.addDate"));
      TestMgr.check(true, content.contains("servicecomb_invocation{operation=\"springmvc.codeFirst.sayHello"));
      TestMgr.check(true, content.contains("servicecomb_invocation{operation=\"springmvc.codeFirst.fallbackFromCache"));
      TestMgr.check(true, content.contains("servicecomb_invocation{operation=\"springmvc.codeFirst.isTrue"));
      TestMgr.check(true, content.contains("servicecomb_invocation{operation=\"springmvc.codeFirst.add"));
      TestMgr.check(true, content.contains("servicecomb_invocation{operation=\"springmvc.codeFirst.sayHi2"));
      TestMgr.check(true, content.contains("servicecomb_invocation{operation=\"springmvc.codeFirst.saySomething"));

      String[] metricLines = content.split("\n");
      if (metricLines.length > 0) {
        for (String metricLine : metricLines) {
          if (!metricLine.startsWith("#")) {
            String[] metricKeyAndValue = metricLine.split(" ");
            if (!metricKeyAndValue[0].startsWith("jvm")) {
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
      TestMgr.check("true", "false");
    }
  }

  private static void testController(RestTemplate template, String microserviceName) {
    String prefix = "cse://" + microserviceName;

    TestMgr.check("hi world [world]",
        template.getForObject(prefix + "/controller/sayhi?name=world",
            String.class));

    TestMgr.check("hi world1 [world1]",
        template.getForObject(prefix + "/controller/sayhi?name={name}",
            String.class,
            "world1"));
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
    TestMgr.check("hi world [world]", controller.sayHi("world"));
    Person user = new Person();
    user.setName("world");
    TestMgr.check("ha world", controller.saySomething("ha", user));
  }
}
