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

package org.apache.servicecomb.demo.springmvc.third;

import java.util.Arrays;

import org.apache.servicecomb.provider.pojo.registry.ThirdServiceWithInvokerRegister;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

/**
 * see: https://github.com/apache/servicecomb-java-chassis/issues/2534
 */
@Component
public class Register extends ThirdServiceWithInvokerRegister {
  public Register() {
    super("third");
    addSchema("heartbeat", HealthSchema.class);
    if (DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.test.vert.transport", true).get()) {
      setUrls("", Arrays.asList("rest://localhost:8080?sslEnabled=false&urlPrefix=%2Fapi"));
    } else {
      setUrls("", Arrays.asList("rest://localhost:8080?sslEnabled=false"));
    }
  }
}
