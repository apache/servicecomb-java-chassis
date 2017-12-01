/*
 *  Copyright 2017 Huawei Technologies Co., Ltd
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.servicecomb.demo.springmvc.tests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.event.ContextClosedEvent;

import io.servicecomb.core.CseApplicationListener;
import io.servicecomb.foundation.common.utils.BeanUtils;

public class RawSpringMvcIntegrationTest extends SpringMvcIntegrationTestBase {

  @BeforeClass
  public static void init() throws Exception {
    System.setProperty("cse.uploads.directory", "/tmp");
    SpringMvcTestMain.main(new String[0]);
  }

  @AfterClass
  public static void shutdown() throws Exception {
    CseApplicationListener cal = BeanUtils.getBean("io.servicecomb.core.CseApplicationListener");
    ContextClosedEvent event = new ContextClosedEvent(BeanUtils.getContext());
    cal.onApplicationEvent(event);
  }
}