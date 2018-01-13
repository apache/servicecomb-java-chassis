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
import java.util.Map.Entry;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.demo.DemoConst;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.controller.Controller;
import org.apache.servicecomb.demo.controller.Person;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.apache.servicecomb.metrics.common.MetricsPublisher;
import org.apache.servicecomb.metrics.common.RegistryMetric;
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

  private static MetricsPublisher metricsPublisher;

  public static void main(String[] args) throws Exception {
    Log4jUtils.init();
    BeanUtils.init();

    run();

    TestMgr.summary();
  }

  public static void run() throws Exception {
    templateUrlWithServiceName.setRequestFactory(new UrlWithServiceNameClientHttpRequestFactory());
    restTemplate = RestTemplateBuilder.create();
    controller = BeanUtils.getBean("controller");
    metricsPublisher = BeanUtils.getBean("metricsPublisher");

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

    //0.5.0 version metrics integration test
    try {
      // this test class is intended for retry hanging issue JAV-127
      String content = restTemplate.getForObject("cse://springmvc/codeFirstSpringmvc/metricsForTest", String.class);
      @SuppressWarnings("unchecked")
      Map<String, String> resultMap = JsonUtils.OBJ_MAPPER.readValue(content, HashMap.class);

      TestMgr.check(true, resultMap.get("CPU and Memory").contains("heapUsed="));

      String[] requestProviders = resultMap.get("totalRequestProvider OPERATIONAL_LEVEL")
          .replace("{", "")
          .replace("}", "")
          .split(",");
      Map<String, Integer> requests = new HashMap<>();
      for (String requestProvider : requestProviders) {
        String[] requestKeyAndValues = requestProvider.split("=");
        requests.put(requestKeyAndValues[0], Integer.parseInt(requestKeyAndValues[1]));
      }

      for (Entry<String, Integer> request : requests.entrySet()) {
        TestMgr.check(true, request.getValue() > 0);
      }

      TestMgr.check(true, resultMap.get("RequestQueueRelated").contains("springmvc.codeFirst.saySomething"));
      TestMgr.check(true, resultMap.get("RequestQueueRelated").contains("springmvc.controller.sayHi"));
    } catch (Exception e) {
      TestMgr.check("true", "false");
    }

    //0.5.0 later version metrics integration test
    try {
      RegistryMetric metric = metricsPublisher.metrics();

      TestMgr
          .check(true, metric.getInstanceMetric().getSystemMetric().getHeapUsed() != 0);
      TestMgr.check(true, metric.getProducerMetrics().size() > 0);
      TestMgr.check(true,
          metric.getProducerMetrics().get("springmvc.codeFirst.saySomething").getProducerCall().getTotal() > 0);
    } catch (Exception e) {
      TestMgr.check("true", "false");
    }

    //prometheus integration test
    try {
      String content = restTemplate.getForObject("cse://springmvc/codeFirstSpringmvc/prometheusForTest", String.class);

      TestMgr.check(true, content.contains("servicecomb_springmvc_codeFirst_addDate"));
      TestMgr.check(true, content.contains("servicecomb_springmvc_codeFirst_sayHello"));
      TestMgr.check(true, content.contains("servicecomb_springmvc_codeFirst_fallbackFromCache"));
      TestMgr.check(true, content.contains("servicecomb_springmvc_codeFirst_isTrue_producer"));
      TestMgr.check(true, content.contains("servicecomb_springmvc_codeFirst_add"));
      TestMgr.check(true, content.contains("servicecomb_springmvc_codeFirst_sayHi2"));
      TestMgr.check(true, content.contains("servicecomb_springmvc_codeFirst_saySomething"));

      String[] metricLines = content.split("\n");
      if (metricLines.length > 0) {
        for (String metricLine : metricLines) {
          if (!metricLine.startsWith("#")) {
            String[] metricKeyAndValue = metricLine.split(" ");
            if (!metricKeyAndValue[0].startsWith("servicecomb_instance_system")) {
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
