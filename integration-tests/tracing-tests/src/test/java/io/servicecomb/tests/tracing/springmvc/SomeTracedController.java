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

package io.servicecomb.tests.tracing.springmvc;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import io.servicecomb.provider.rest.common.RestSchema;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestSchema(schemaId = "someTracedRestEndpoint")
@RestController
@RequestMapping("/")
public class SomeTracedController {
  private static final Logger logger = LoggerFactory.getLogger(SomeTracedController.class);

  @Bean
  RestTemplate template() {
    return new RestTemplate();
  }

  @Autowired
  private RestTemplate template;

  @RequestMapping(value = "/hello", method = GET, produces = TEXT_PLAIN_VALUE)
  public String hello(HttpServletRequest request) throws InterruptedException {
    logger.info("in /hello");
    Random random = new Random();
    Thread.sleep(random.nextInt(1000));

    return "hello " + template.getForObject("http://localhost:8080/world", String.class);
  }

  @RequestMapping(value = "/world", method = GET, produces = TEXT_PLAIN_VALUE)
  public String world() throws InterruptedException {
    logger.info("in /world");
    Random random = new Random();
    Thread.sleep(random.nextInt(1000));

    return "world";
  }
}