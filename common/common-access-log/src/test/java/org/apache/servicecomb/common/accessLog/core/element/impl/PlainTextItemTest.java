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

import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class PlainTextItemTest {
  private InvocationFinishEvent finishEvent;

  private ServerAccessLogEvent accessLogEvent;

  private StringBuilder strBuilder;

  @BeforeEach
  public void initStrBuilder() {
    accessLogEvent = new ServerAccessLogEvent();
    finishEvent = Mockito.mock(InvocationFinishEvent.class);
    strBuilder = new StringBuilder();
  }

  @Test
  public void serverFormattedElement() {
    PlainTextAccessItem element = new PlainTextAccessItem("contentTest");
    element.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("contentTest", strBuilder.toString());
  }

  @Test
  public void clientFormattedElement() {
    PlainTextAccessItem element = new PlainTextAccessItem("contentTest");
    element.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("contentTest", strBuilder.toString());
  }
}
