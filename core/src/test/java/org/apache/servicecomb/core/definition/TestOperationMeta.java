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

package org.apache.servicecomb.core.definition;

import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.core.unittest.UnitTestMeta;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.swagger.extend.annotations.ResponseHeaders;
import org.apache.servicecomb.swagger.invocation.response.ResponseMeta;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ResponseHeader;

public class TestOperationMeta {
  class Impl {
    @ApiResponse(
        code = 300,
        response = String.class,
        message = "",
        responseHeaders = {@ResponseHeader(name = "h3", response = int.class)})
    @ResponseHeaders({@ResponseHeader(name = "h1", response = int.class),
        @ResponseHeader(name = "h2", response = String.class, responseContainer = "List")})
    public int test(int x) {
      return 100;
    }
  }

  @BeforeClass
  public static void setup() {
    ArchaiusUtils.resetConfig();
  }

  @AfterClass
  public static void teardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testOperationMeta() {
    UnitTestMeta meta = new UnitTestMeta();
    SchemaMeta schemaMeta = meta.getOrCreateSchemaMeta(Impl.class);
    OperationMeta operationMeta = schemaMeta.findOperation("test");

    Assert.assertEquals("POST", operationMeta.getHttpMethod());
    Assert.assertEquals("/test", operationMeta.getOperationPath());
    Assert.assertEquals(schemaMeta, operationMeta.getSchemaMeta());
    Assert.assertEquals(Impl.class.getName() + ".test",
        operationMeta.getSchemaQualifiedName());
    Assert.assertEquals("perfClient." + Impl.class.getName() + ".test",
        operationMeta.getMicroserviceQualifiedName());
    Assert.assertEquals("perfClient", operationMeta.getMicroserviceName());
    Assert.assertEquals("test", operationMeta.getOperationId());
    Assert.assertEquals("x", operationMeta.getParamName(0));

    operationMeta.putExtData("ext", 1);
    Assert.assertEquals(1, (int) operationMeta.getExtData("ext"));

    ResponseMeta responseMeta = operationMeta.findResponseMeta(200);
    Assert.assertEquals("Ljava/lang/Integer;", responseMeta.getJavaType().getGenericSignature());
    Assert.assertEquals("Ljava/lang/Integer;", responseMeta.getHeaders().get("h1").getGenericSignature());
    Assert.assertEquals("Ljava/util/List<Ljava/lang/String;>;",
        responseMeta.getHeaders().get("h2").getGenericSignature());
    Assert.assertEquals(null, responseMeta.getHeaders().get("h3"));

    responseMeta = operationMeta.findResponseMeta(300);
    Assert.assertEquals("Ljava/lang/String;", responseMeta.getJavaType().getGenericSignature());
    Assert.assertEquals("Ljava/lang/Integer;", responseMeta.getHeaders().get("h1").getGenericSignature());
    Assert.assertEquals("Ljava/util/List<Ljava/lang/String;>;",
        responseMeta.getHeaders().get("h2").getGenericSignature());
    Assert.assertEquals("Ljava/lang/Integer;", responseMeta.getHeaders().get("h3").getGenericSignature());
  }

  @Test
  public void opConfig() {
    UnitTestMeta meta = new UnitTestMeta();
    SchemaMeta schemaMeta = meta.getOrCreateSchemaMeta(Impl.class);
    OperationMeta operationMeta = schemaMeta.findOperation("test");

    OperationConfig config = operationMeta.getConfig();

    // slow invocation
    slowInvocation(config);

    // consumer request timeout
    consumerRequestTimeout(config);

    // highway wait in thread pool timeout
    highwayWaitInPool(config);

    // rest wait in thread pool timeout
    restWaitInPool(config);
  }

  private void restWaitInPool(OperationConfig config) {
    ArchaiusUtils.setProperty("servicecomb.Provider.requestWaitInPoolTimeout", null);
    ArchaiusUtils.setProperty("servicecomb.Provider.requestWaitInPoolTimeout.perfClient", null);
    ArchaiusUtils.setProperty(
        "servicecomb.Provider.requestWaitInPoolTimeout.perfClient.org.apache.servicecomb.core.definition.TestOperationMeta$Impl",
        null);
    ArchaiusUtils.setProperty(
        "servicecomb.Provider.requestWaitInPoolTimeout.perfClient.org.apache.servicecomb.core.definition.TestOperationMeta$Impl.test",
        null);

    Assert.assertEquals(30000, config.getMsRestRequestWaitInPoolTimeout());
    Assert.assertEquals(TimeUnit.MILLISECONDS.toNanos(30000), config.getNanoRestRequestWaitInPoolTimeout());

    ArchaiusUtils.setProperty("servicecomb.Provider.requestWaitInPoolTimeout", 1);
    Assert.assertEquals(1, config.getMsRestRequestWaitInPoolTimeout());
    Assert.assertEquals(TimeUnit.MILLISECONDS.toNanos(1), config.getNanoRestRequestWaitInPoolTimeout());

    ArchaiusUtils.setProperty("servicecomb.Provider.requestWaitInPoolTimeout.perfClient", 2);
    Assert.assertEquals(2, config.getMsRestRequestWaitInPoolTimeout());
    Assert.assertEquals(TimeUnit.MILLISECONDS.toNanos(2), config.getNanoRestRequestWaitInPoolTimeout());

    ArchaiusUtils.setProperty(
        "servicecomb.Provider.requestWaitInPoolTimeout.perfClient.org.apache.servicecomb.core.definition.TestOperationMeta$Impl",
        3);
    Assert.assertEquals(3, config.getMsRestRequestWaitInPoolTimeout());
    Assert.assertEquals(TimeUnit.MILLISECONDS.toNanos(3), config.getNanoRestRequestWaitInPoolTimeout());

    ArchaiusUtils.setProperty(
        "servicecomb.Provider.requestWaitInPoolTimeout.perfClient.org.apache.servicecomb.core.definition.TestOperationMeta$Impl.test",
        4);
    Assert.assertEquals(4, config.getMsRestRequestWaitInPoolTimeout());
    Assert.assertEquals(TimeUnit.MILLISECONDS.toNanos(4), config.getNanoRestRequestWaitInPoolTimeout());
  }

  private void highwayWaitInPool(OperationConfig config) {
    Assert.assertEquals(30000, config.getMsHighwayRequestWaitInPoolTimeout());
    Assert.assertEquals(TimeUnit.MILLISECONDS.toNanos(30000), config.getNanoHighwayRequestWaitInPoolTimeout());

    ArchaiusUtils.setProperty("servicecomb.Provider.requestWaitInPoolTimeout", 1);
    Assert.assertEquals(1, config.getMsHighwayRequestWaitInPoolTimeout());
    Assert.assertEquals(TimeUnit.MILLISECONDS.toNanos(1), config.getNanoHighwayRequestWaitInPoolTimeout());

    ArchaiusUtils.setProperty("servicecomb.Provider.requestWaitInPoolTimeout.perfClient", 2);
    Assert.assertEquals(2, config.getMsHighwayRequestWaitInPoolTimeout());
    Assert.assertEquals(TimeUnit.MILLISECONDS.toNanos(2), config.getNanoHighwayRequestWaitInPoolTimeout());

    ArchaiusUtils.setProperty(
        "servicecomb.Provider.requestWaitInPoolTimeout.perfClient.org.apache.servicecomb.core.definition.TestOperationMeta$Impl",
        3);
    Assert.assertEquals(3, config.getMsHighwayRequestWaitInPoolTimeout());
    Assert.assertEquals(TimeUnit.MILLISECONDS.toNanos(3), config.getNanoHighwayRequestWaitInPoolTimeout());

    ArchaiusUtils.setProperty(
        "servicecomb.Provider.requestWaitInPoolTimeout.perfClient.org.apache.servicecomb.core.definition.TestOperationMeta$Impl.test",
        4);
    Assert.assertEquals(4, config.getMsHighwayRequestWaitInPoolTimeout());
    Assert.assertEquals(TimeUnit.MILLISECONDS.toNanos(4), config.getNanoHighwayRequestWaitInPoolTimeout());
  }

  private void consumerRequestTimeout(OperationConfig config) {
    Assert.assertEquals(30000, config.getMsRequestTimeout());

    ArchaiusUtils.setProperty("servicecomb.request.perfClient.timeout", 2);
    Assert.assertEquals(2, config.getMsRequestTimeout());
    ArchaiusUtils.setProperty(
        "servicecomb.request.perfClient.org.apache.servicecomb.core.definition.TestOperationMeta$Impl.timeout", 3);
    Assert.assertEquals(3, config.getMsRequestTimeout());
    ArchaiusUtils.setProperty(
        "servicecomb.request.perfClient.org.apache.servicecomb.core.definition.TestOperationMeta$Impl.test.timeout", 4);
    Assert.assertEquals(4, config.getMsRequestTimeout());
  }

  private void slowInvocation(OperationConfig config) {
    Assert.assertFalse(config.isSlowInvocationEnabled());
    Assert.assertEquals(1000, config.getMsSlowInvocation());
    Assert.assertEquals(TimeUnit.MILLISECONDS.toNanos(1000), config.getNanoSlowInvocation());

    ArchaiusUtils.setProperty("servicecomb.Consumer.invocation.slow.enabled.perfClient", true);
    ArchaiusUtils.setProperty("servicecomb.Consumer.invocation.slow.msTime.perfClient", 2000);
    Assert.assertTrue(config.isSlowInvocationEnabled());
    Assert.assertEquals(2000, config.getMsSlowInvocation());
    Assert.assertEquals(TimeUnit.MILLISECONDS.toNanos(2000), config.getNanoSlowInvocation());

    // new configuration key, has high priority
    ArchaiusUtils.setProperty("servicecomb.metrics.Consumer.invocation.slow.enabled", false);
    ArchaiusUtils.setProperty("servicecomb.metrics.Consumer.invocation.slow.msTime", 3000);
    Assert.assertFalse(config.isSlowInvocationEnabled());
    Assert.assertEquals(3000, config.getMsSlowInvocation());
    Assert.assertEquals(TimeUnit.MILLISECONDS.toNanos(3000), config.getNanoSlowInvocation());

    ArchaiusUtils.setProperty("servicecomb.metrics.Consumer.invocation.slow.enabled.perfClient", true);
    ArchaiusUtils.setProperty("servicecomb.metrics.Consumer.invocation.slow.msTime.perfClient", 4000);
    Assert.assertTrue(config.isSlowInvocationEnabled());
    Assert.assertEquals(4000, config.getMsSlowInvocation());
    Assert.assertEquals(TimeUnit.MILLISECONDS.toNanos(4000), config.getNanoSlowInvocation());
  }
}
