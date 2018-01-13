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

package org.apache.servicecomb.metrics.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.metrics.common.HealthCheckResult;
import org.apache.servicecomb.metrics.common.HealthCheckerPublisher;
import org.apache.servicecomb.metrics.core.publish.DefaultHealthCheckerPublisher;
import org.apache.servicecomb.metrics.core.publish.HealthCheckerManager;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

public class TestHealthCheckerPublisher {

  @Test
  public void testPublisher() throws JsonProcessingException {
    HealthCheckerManager manager = mock(HealthCheckerManager.class);

    Map<String, HealthCheckResult> results = new HashMap<>();
    HealthCheckResult result = new HealthCheckResult(true, "ok", "extra");
    results.put("default", result);

    when(manager.check()).thenReturn(results);
    when(manager.check("default")).thenReturn(result);

    HealthCheckerPublisher publisher = new DefaultHealthCheckerPublisher(manager);
    Map<String, HealthCheckResult> content = publisher.health();
    Assert.assertTrue(JsonUtils.writeValueAsString(content.get("default"))
        .equals(JsonUtils.writeValueAsString(result)));

    HealthCheckResult checkResult = publisher.healthWithName("default");
    Assert.assertTrue(JsonUtils.writeValueAsString(checkResult)
        .equals(JsonUtils.writeValueAsString(result)));
  }
}
