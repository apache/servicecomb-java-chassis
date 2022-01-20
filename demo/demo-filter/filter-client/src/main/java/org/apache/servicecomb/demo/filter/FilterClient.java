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

package org.apache.servicecomb.demo.filter;

import org.apache.servicecomb.demo.CategorizedTestCaseRunner;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(FilterClient.class);

  public static void main(String[] args) throws Exception {
    try {
      Log4jUtils.init();
      BeanUtils.init();

      run();
    } catch (Throwable e) {
      TestMgr.check("success", "failed");
      LOGGER.error("-------------- test failed -------------");
      LOGGER.error("", e);
      LOGGER.error("-------------- test failed -------------");
    }
    TestMgr.summary();
  }

  public static void run() throws Exception {
    CategorizedTestCaseRunner.runCategorizedTestCase("filterServer");
  }
}
