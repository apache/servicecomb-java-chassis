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

package org.apache.servicecomb.springboot.springmvc.server;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.springboot2.starter.EnableServiceComb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.netflix.config.DynamicPropertyFactory;

@SpringBootApplication
@EnableServiceComb
public class SpringmvcServer {
  private static final Logger LOGGER = LoggerFactory.getLogger(SpringmvcServer.class);

  public static void main(final String[] args) throws Exception {
    new SpringApplicationBuilder().sources(SpringmvcServer.class).web(WebApplicationType.NONE).build().run(args);

    assertPropertyCorrect();
  }

  private static void assertPropertyCorrect() {
    String result = DynamicPropertyFactory.getInstance()
        .getStringProperty("test.unresolved.placeholder", null).get();
    if (!"jdbc:postgresql://${ip}:${port}/pt".equals(result)) {
      LOGGER.error("tests for configuration error, stop");
      SCBEngine.getInstance().destroy();
    }
  }
}
