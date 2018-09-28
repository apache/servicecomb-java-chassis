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

package org.apache.servicecomb.it;

import org.apache.servicecomb.springboot2.starter.EnableServiceComb;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableServiceComb
public class SpringBoot2ServletApplication {

  /**
   * Allow encoded slash in path param(%2F).
   * This property is set for {@code stringPath} tests in {@code TestDataTypePrimitive}
   */
  private static final String TOMCAT_ALLOW_ENCODED_SLASH = "org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH";

  public static void main(String[] args) {
    new CommandReceiver();

    System.setProperty(TOMCAT_ALLOW_ENCODED_SLASH, "true");
    SpringApplication.run(SpringBoot2ServletApplication.class, args);
    System.clearProperty(TOMCAT_ALLOW_ENCODED_SLASH);
  }
}
