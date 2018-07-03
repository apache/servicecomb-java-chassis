package org.apache.servicecomb.demo.springmvc.client;
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

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.springframework.stereotype.Component;

/**
 * Testing after bootup.
 */
@Component
public class ConsumerTestsAfterBootup implements BootListener {
  public void testRegisterPath() {
    TestMgr.check(RegistryUtils.getMicroservice().getPaths().size(), 0);
  }

  @Override
  public void onBootEvent(BootEvent event) {
    if (event.getEventType() == BootListener.EventType.AFTER_REGISTRY) {
      testRegisterPath();
      if (!TestMgr.isSuccess()) {
        TestMgr.summary();
        throw new IllegalStateException("some tests are failed. ");
      }
    }
  }
}
