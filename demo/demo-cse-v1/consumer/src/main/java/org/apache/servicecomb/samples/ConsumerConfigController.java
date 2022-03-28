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

package org.apache.servicecomb.samples;

import java.util.List;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.netflix.config.DynamicPropertyFactory;

@RestSchema(schemaId = "ConsumerConfigController")
@RequestMapping(path = "/")
public class ConsumerConfigController {
  private Environment environment;

  private ConsumerConfigurationProperties consumerConfigurationProperties;

  @Autowired
  public ConsumerConfigController(Environment environment, ConsumerConfigurationProperties consumerConfigurationProperties) {
    this.environment = environment;
    this.consumerConfigurationProperties = consumerConfigurationProperties;
  }

  @GetMapping("/config")
  public String config(@RequestParam("key") String key) {
    return environment.getProperty(key);
  }

  @GetMapping("/foo")
  public String foo() {
    return consumerConfigurationProperties.getFoo();
  }

  @GetMapping("/bar")
  public String bar() {
    return consumerConfigurationProperties.getBar();
  }

  @GetMapping("/dynamicString")
  public String dynamicString(@RequestParam("key") String key) {
    return DynamicPropertyFactory.getInstance().getStringProperty(key, null).get();
  }

  @GetMapping("/dynamicArray")
  @SuppressWarnings("unchecked")
  public List<String> dynamicArray() {
    return consumerConfigurationProperties.getDynamicArray();
//     DynamicPropertyFactory & Environment do not support arrays like:
//           key[0]: v0
//           key[1]: v1
//    return environment.getProperty(key, List.class);
//    return Arrays.asList(((AbstractConfiguration) DynamicPropertyFactory.getBackingConfigurationSource())
//        .getStringArray(key));
  }
}
