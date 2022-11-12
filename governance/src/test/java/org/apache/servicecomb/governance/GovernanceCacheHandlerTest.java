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


import org.apache.servicecomb.governance.handler.GovernanceCacheHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.GovernanceCachePolicy;
import org.apache.servicecomb.governance.service.GovernanceCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {GovernanceConfiguration.class, MockConfiguration.class})
public class GovernanceCacheHandlerTest {
  private GovernanceCacheHandler<String, Object> governanceCacheHandler;

  @Autowired
  public void setInstanceIsolationHandler(@Autowired GovernanceCacheHandler<String, Object> governanceCacheHandler) {
    this.governanceCacheHandler = governanceCacheHandler;
  }

  @Test
  public void testMatchPriorityPolicy() {
    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/governanceCache");
    GovernanceCachePolicy policy = governanceCacheHandler.matchPolicy(request);
    Assertions.assertEquals("demo-governanceCache", policy.getName());
    GovernanceCache<String, Object> governanceCache = governanceCacheHandler.getActuator(request);
    governanceCache.putValueIntoCache("governance", "Cache");
    Object cache = governanceCache.getValueFromCache("governance");
    Assertions.assertEquals("Cache", cache);
    governanceCache.putValueIntoCache("response", null);
    Object response = governanceCache.getValueFromCache("response");
    Assertions.assertNull(response);
  }
}
