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

package org.apache.servicecomb.archetypes;

import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TestConsumer {

  private String setting;

  //use local registry for test consumer
  //more details can be found :
  // http://servicecomb.incubator.apache.org/users/develop-with-rest-template/
  // http://servicecomb.incubator.apache.org/users/develop-with-rpc/
  @Before
  public void setUp() {
    setting = System.getProperty("local.registry.file");
    System.setProperty("local.registry.file",
        TestConsumer.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "localregistry.yaml");
  }

  @Test
  public void test() throws Exception {
    //start provider
    Log4jUtils.init();
    BeanUtils.init();

    //consumer call
    RestTemplate restTemplate = RestTemplateBuilder.create();
    String result = restTemplate.getForObject("cse://business-service/hello", String.class);
    Assert.assertEquals("Hello World!", result);
  }

  //recovery local.registry.file setting
  @After
  public void tearDown() {
    System.setProperty("local.registry.file", setting == null ? "" : setting);
  }
}
