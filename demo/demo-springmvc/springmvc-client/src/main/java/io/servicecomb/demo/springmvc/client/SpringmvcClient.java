/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.demo.springmvc.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import io.servicecomb.core.CseContext;
import io.servicecomb.demo.DemoConst;
import io.servicecomb.demo.TestMgr;
import io.servicecomb.demo.controller.Controller;
import io.servicecomb.demo.controller.Person;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.Log4jUtils;
import io.servicecomb.provider.springmvc.reference.CseRestTemplate;
import io.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import io.servicecomb.provider.springmvc.reference.UrlWithServiceNameClientHttpRequestFactory;

public class SpringmvcClient {
  private static RestTemplate templateUrlWithServiceName = new CseRestTemplate();

  private static RestTemplate restTemplate;

  private static Controller controller;

  public static void main(String[] args) throws Exception {
    templateUrlWithServiceName.setRequestFactory(new UrlWithServiceNameClientHttpRequestFactory());
    Log4jUtils.init();
    BeanUtils.init();

    run();

    TestMgr.summary();
  }

  public static void run() throws Exception {
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
