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

package org.apache.servicecomb.demo.discovery.zuul;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.apache.servicecomb.springboot.starter.provider.EnableServiceComb;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DiscoveryZuulProxyIT.DiscoveryZuulProxy.class, webEnvironment = RANDOM_PORT)
public class DiscoveryZuulProxyIT {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  public void getsRemoteServiceThroughGateway() {
    //loop three time to insure only rest endpoint get
    for (int i = 0; i < 3; i++) {
      String response = restTemplate.getForObject(
          "/gateway/greeting/sayhello/{name}",
          String.class,
          "Mike");

      assertThat(response).isEqualTo("hello Mike");
    }
  }

  @SpringBootApplication
  @EnableZuulProxy
  @EnableDiscoveryClient
  @EnableServiceComb
  static class DiscoveryZuulProxy {

    public static void main(String[] args) {
      SpringApplication.run(DiscoveryZuulProxy.class, args);
    }
  }
}
