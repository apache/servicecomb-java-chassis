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

import java.time.Duration;

import org.apache.servicecomb.governance.handler.TimeLimiterHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.TimeLimiterPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import io.github.resilience4j.timelimiter.TimeLimiter;

@SpringBootTest
@ContextConfiguration(classes = {GovernanceConfiguration.class, MockConfiguration.class})
public class TimeLimiterHandlerTest {
  private TimeLimiterHandler timeLimiterHandler;

  @Autowired
  public void setInstanceIsolationHandler(TimeLimiterHandler timeLimiterHandler) {
    this.timeLimiterHandler = timeLimiterHandler;
  }

  @Test
  public void testMatchPriorityPolicy() {
    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/timeLimiter");
    TimeLimiterPolicy policy = timeLimiterHandler.matchPolicy(request);
    Assertions.assertEquals("demo-timeLimiter", policy.getName());
    TimeLimiter timeLimiter = timeLimiterHandler.getActuator(request);
    Duration timeoutDuration = timeLimiter.getTimeLimiterConfig().getTimeoutDuration();
    Assertions.assertEquals(2000, timeoutDuration.toMillis());
  }
}
