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

package org.apache.servicecomb.common.rest;

import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class TestCommonRestConfig {
  @After
  public void tearDown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testTimeoutConfig() {
    Assert.assertEquals(CommonRestConfig.getRequestWaitInPoolTimeout(), 30000);
    ArchaiusUtils.setProperty(CommonRestConfig.KEY_SERVICECOMB_REQUEST_WAIT_IN_POOL_TIMEOUT, 50000);
    Assert.assertEquals(CommonRestConfig.getRequestWaitInPoolTimeout(), 50000);
  }
}
