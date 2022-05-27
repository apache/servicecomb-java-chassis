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

package org.apache.servicecomb.demo;

import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategorizedTestCaseRunner {
  private static final Logger LOGGER = LoggerFactory.getLogger(CategorizedTestCaseRunner.class);

  public static void runCategorizedTestCase(String microserviceName) throws Exception {
    Map<String, CategorizedTestCase> tests = BeanUtils.getContext().getBeansOfType(CategorizedTestCase.class);
    for (String transport : DemoConst.transports) {
      for (CategorizedTestCase testCase : tests.values()) {

        try {
          if (testCase.getMicroserviceName() != null) {
            changeTransport(testCase.getMicroserviceName(), transport);
          } else {
            changeTransport(microserviceName, transport);
          }

          testCase.testAllTransport();
          if ("rest".equals(transport)) {
            testCase.testRestTransport();
          } else if ("highway".equals(transport)) {
            testCase.testHighwayTransport();
          }
        } catch (Exception e) {
          LOGGER.error("run categorized test case " +
              testCase.getClass().getName() +
              " failed.", e);
          TestMgr.failed("run categorized test case " +
                  testCase.getClass().getName() +
                  " failed, reason " + e.getMessage(),
              e);
        }
      }
    }
  }

  public static void changeTransport(String microserviceName, String transport) {
    ArchaiusUtils.setProperty("servicecomb.references.transport." + microserviceName, transport);
    TestMgr.setMsg(microserviceName, transport);
  }
}
