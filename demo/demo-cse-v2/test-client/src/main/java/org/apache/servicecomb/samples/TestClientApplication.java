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

import org.apache.servicecomb.demo.CategorizedTestCaseRunner;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.springboot2.starter.EnableServiceComb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@EnableServiceComb
public class TestClientApplication {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestClientApplication.class);

  public static void main(String[] args) throws Exception {
    try {
      new SpringApplicationBuilder().web(WebApplicationType.NONE).sources(TestClientApplication.class).run(args);

      CategorizedTestCaseRunner.runCategorizedTestCase("consumer");
    } catch (Exception e) {
      TestMgr.failed("test case run failed", e);
      LOGGER.error("-------------- test failed -------------");
      LOGGER.error("", e);
      LOGGER.error("-------------- test failed -------------");
    }
    TestMgr.summary();
  }
}
