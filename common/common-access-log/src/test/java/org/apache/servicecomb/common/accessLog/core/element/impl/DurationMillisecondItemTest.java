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

package org.apache.servicecomb.common.accessLog.core.element.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DurationMillisecondItemTest {

  public static final DurationMillisecondItemAccess ELEMENT = new DurationMillisecondItemAccess();

  private StringBuilder strBuilder;

  private InvocationFinishEvent finishEvent;

  private ServerAccessLogEvent accessLogEvent;

  private Invocation invocation;

  private InvocationStageTrace invocationStageTrace;

  @Before
  public void initStrBuilder() {
    finishEvent = Mockito.mock(InvocationFinishEvent.class);
    invocation = Mockito.mock(Invocation.class);
    invocationStageTrace = Mockito.mock(InvocationStageTrace.class);

    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getInvocationStageTrace()).thenReturn(invocationStageTrace);
    when(invocationStageTrace.getStartSend()).thenReturn(0L);
    when(invocationStageTrace.getFinish()).thenReturn(1000_000L);

    accessLogEvent = new ServerAccessLogEvent();
    accessLogEvent.setMilliStartTime(1L);
    accessLogEvent.setMilliEndTime(2L);
    strBuilder = new StringBuilder();
  }

  @Test
  public void testAppendFormattedElement() {
    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    assertEquals("1", strBuilder.toString());

    strBuilder = new StringBuilder();
    ELEMENT.appendClientFormattedItem(finishEvent, strBuilder);
    assertEquals("1", strBuilder.toString());
  }
}
