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

package org.apache.servicecomb.samples.localregistry.localregistryclient;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.samples.common.schema.models.Person;
import org.springframework.web.client.RestTemplate;

public class LocalRegistryClient {
  private static RestTemplate templateNew = RestTemplateBuilder.create();

  public static void main(String[] args) throws Exception {
    System.setProperty("local.registry.file", "src/main/resources/registry.yaml");
    init();

    run();
  }

  public static void init() throws Exception {
    Log4jUtils.init();
    BeanUtils.init();
  }

  public static void run() throws Exception {
    testLocalRegistry(templateNew);
  }

  private static void testLocalRegistry(RestTemplate template) throws Exception {
    String microserviceName = "localserv";
    String cseUrlPrefix = "cse://" + microserviceName + "/localservregistry/";

    Map<String, String> params = new HashMap<>();
    params.put("a", "5");
    params.put("b", "20");
    int result = template.postForObject(cseUrlPrefix + "add", params, Integer.class);
    System.out.println(result);

    Person person = new Person();
    person.setName("local registry test");
    Person sayHiResult = template
        .postForObject(
            cseUrlPrefix + "sayhi",
            person,
            Person.class);

    System.out.println(sayHiResult.getName());
  }
}
