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

package org.apache.servicecomb.tracing.zipkin;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.core.Invocation;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import brave.propagation.Propagation.Getter;
import mockit.Deencapsulation;

public class ZipkinProviderDelegateTest {
  @Test
  public void testGetterOnGetSpanId() {
    Getter<Invocation, String> getter = Deencapsulation
        .getField(ZipkinProviderDelegate.class, "INVOCATION_STRING_GETTER");

    Invocation invocation = Mockito.mock(Invocation.class);
    Map<String, String> context = new HashMap<>();

    Mockito.when(invocation.getContext()).thenReturn(context);

    // if there is no spanId or traceId, then result is null
    String spanId = getter.get(invocation, ZipkinProviderDelegate.SPAN_ID_HEADER_NAME);
    Assert.assertNull(spanId);

    // if there is no spanId but traceId, then traceId will be returned as result
    final String testTraceId = "testTraceId";
    context.put(ZipkinProviderDelegate.TRACE_ID_HEADER_NAME, testTraceId);
    spanId = getter.get(invocation, ZipkinProviderDelegate.SPAN_ID_HEADER_NAME);
    Assert.assertEquals(testTraceId, spanId);

    // if there is spanId, then spanId will be returned
    final String testSpanId = "testSpanId";
    context.put(ZipkinProviderDelegate.SPAN_ID_HEADER_NAME, testSpanId);
    spanId = getter.get(invocation, ZipkinProviderDelegate.SPAN_ID_HEADER_NAME);
    Assert.assertEquals(testSpanId, spanId);
  }

  @Test
  public void testGetterOnGetOtherContent() {
    Getter<Invocation, String> getter = Deencapsulation
        .getField(ZipkinProviderDelegate.class, "INVOCATION_STRING_GETTER");

    Invocation invocation = Mockito.mock(Invocation.class);
    Map<String, String> context = new HashMap<>();

    Mockito.when(invocation.getContext()).thenReturn(context);

    final String key = "key";
    String value = getter.get(invocation, key);
    Assert.assertNull(value);

    final String expectedValue = "value";
    context.put(key, expectedValue);
    value = getter.get(invocation, key);
    Assert.assertEquals(expectedValue, value);
  }
}
