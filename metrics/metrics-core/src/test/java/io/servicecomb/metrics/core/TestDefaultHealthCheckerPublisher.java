/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.metrics.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.servicecomb.foundation.common.utils.JsonUtils;
import io.servicecomb.metrics.core.health.HealthCheckResult;
import io.servicecomb.metrics.core.provider.DefaultHealthCheckerPublisher;
import io.servicecomb.metrics.core.provider.HealthCheckerPublisher;
import io.servicecomb.metrics.core.registry.HealthCheckRegistry;

public class TestDefaultHealthCheckerPublisher {

  @Test
  public void testPublisher() throws JsonProcessingException {
    HealthCheckRegistry registry = mock(HealthCheckRegistry.class);

    Map<String, HealthCheckResult> results = new HashMap<>();
    HealthCheckResult result = new HealthCheckResult(true, "ok", "extra");
    results.put("default", result);

    when(registry.checkAllStatus()).thenReturn(results);
    when(registry.checkStatus("default")).thenReturn(result);

    HealthCheckerPublisher publisher = new DefaultHealthCheckerPublisher(registry);
    Map<String, String> content = publisher.health();
    Assert.assertTrue(content.get("default").equals(JsonUtils.writeValueAsString(result)));

    String content2 = publisher.health("default");
    Assert.assertTrue(content2.equals(JsonUtils.writeValueAsString(result)));
  }
}
