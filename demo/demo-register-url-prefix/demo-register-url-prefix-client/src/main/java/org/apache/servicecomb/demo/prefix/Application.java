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

package org.apache.servicecomb.demo.prefix;

import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.springboot2.starter.EnableServiceComb;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableServiceComb
public class Application {
  public static void main(final String[] args) throws Exception {
    new SpringApplicationBuilder().sources(Application.class).web(WebApplicationType.SERVLET).build().run(args);

    runTest();
  }

  public static void runTest() {
    RestTemplate template = RestTemplateBuilder.create();
    TestMgr.check("2", template
        .getForObject("cse://demo-register-url-prefix-server/hellodemo/register/url/prefix/getName?name=2",
            String.class));
    TestMgr.summary();
  }
}
