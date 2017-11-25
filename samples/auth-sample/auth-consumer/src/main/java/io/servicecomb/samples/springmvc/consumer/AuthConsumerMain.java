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
package io.servicecomb.samples.springmvc.consumer;

import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.Log4jUtils;
import io.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import io.servicecomb.samples.common.schema.models.Person;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthConsumerMain {

  private static RestTemplate restTemplate = RestTemplateBuilder.create();

  public static void main(String[] args) throws Exception {
    init();
    Person person = new Person();
    person.setName("ServiceComb/Authenticate");
    System.out
        .println("RestTemplate Consumer or POJO Consumer.  You can choose whatever you like.");
    String sayHiResult = restTemplate
        .postForObject(
            "cse://auth-provider/springmvchello/sayhi?name=Authenticate",
            null,
            String.class);
    String sayHelloResult = restTemplate.postForObject(
        "cse://auth-provider/springmvchello/sayhello",
        person,
        String.class);
    Assert.isTrue("Hello Authenticate".equals(sayHiResult));
    Assert.isTrue("Hello person ServiceComb/Authenticate".equals(sayHelloResult));
  }

  public static void init() throws Exception {
    Log4jUtils.init();
    BeanUtils.init();
  }
}
