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

package io.servicecomb.foundation.metrics.performance;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.foundation.metrics.MetricsServoRegistry;
import io.servicecomb.swagger.invocation.InvocationType;

public class TestMetricsDataMonitorUtil {
  MetricsDataMonitor metricsDataMonitor = null;

  MetricsDataMonitorUtil metricsDataMonitorUtil = null;

  @Before
  public void setUp() throws Exception {
    metricsDataMonitor = MetricsServoRegistry.getOrCreateLocalMetrics();
    metricsDataMonitorUtil = new MetricsDataMonitorUtil();
  }

  @After
  public void tearDown() throws Exception {
    metricsDataMonitor = null;
  }

  @Test
  public void testAllReqConsumer() {

    Invocation invocation = Mockito.mock(Invocation.class);
    OperationMeta operationMetaData = Mockito.mock(OperationMeta.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMetaData);
    Mockito.when(operationMetaData.getMicroserviceQualifiedName()).thenReturn("/sayHi");
    Mockito.when(invocation.getMicroserviceQualifiedName()).thenReturn("/sayHi");
    Mockito.when(invocation.getInvocationType()).thenReturn(InvocationType.CONSUMER);

    metricsDataMonitorUtil.setAllReqProviderAndConsumer(invocation);
    Assert.assertNotEquals(0L, metricsDataMonitor.getTotalReqConsumer());
    metricsDataMonitorUtil.setAllFailReqProviderAndConsumer(invocation);
    Assert.assertNotEquals(0L, metricsDataMonitor.getTotalFailReqConsumer());
  }

  @Test
  public void testAllReqProvider() {

    Invocation invocation = Mockito.mock(Invocation.class);
    OperationMeta operationMetaData = Mockito.mock(OperationMeta.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMetaData);
    Mockito.when(operationMetaData.getMicroserviceQualifiedName()).thenReturn("/sayBye");
    Mockito.when(invocation.getMicroserviceQualifiedName()).thenReturn("sayBye");
    Mockito.when(invocation.getInvocationType()).thenReturn(InvocationType.PRODUCER);

    metricsDataMonitorUtil.setAllReqProviderAndConsumer(invocation);
    Assert.assertNotEquals(0L, metricsDataMonitor.getTotalReqProvider());
    metricsDataMonitorUtil.setAllReqProviderAndConsumer(invocation);
    Assert.assertNotEquals(0L, metricsDataMonitor.getTotalReqProvider());
    metricsDataMonitorUtil.setAllFailReqProviderAndConsumer(invocation);
    Assert.assertNotEquals(0L, metricsDataMonitor.getTotalFailReqProvider());
    metricsDataMonitorUtil.setAllFailReqProviderAndConsumer(invocation);
    Assert.assertNotEquals(0L, metricsDataMonitor.getTotalFailReqProvider());
  }
}
