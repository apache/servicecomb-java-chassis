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

package org.apache.servicecomb.governance;

import org.apache.servicecomb.governance.handler.BulkheadHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.BulkheadPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {GovernanceConfiguration.class, MockConfiguration.class})
public class BulkheadHandlerTest {
  private BulkheadHandler bulkheadHandler;

  @Autowired
  public void setInstanceIsolationHandler(BulkheadHandler bulkheadHandler) {
    this.bulkheadHandler = bulkheadHandler;
  }

  @Test
  public void testMatchPriorityPolicy() {
    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/bulkhead");
    BulkheadPolicy policy = bulkheadHandler.matchPolicy(request);
    Assertions.assertEquals("demo-bulkhead-priority", policy.getName());
  }
}
