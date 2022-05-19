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

package org.apache.servicecomb.foundation.logback;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Marker;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

public class TestMarkerFilter {
  @Test
  public void testMarkerFilter() {
    MarkerFilter filter = new MarkerFilter();
    filter.setMarker("hello");
    filter.start();
    Assertions.assertEquals(filter.getOnMatch(), FilterReply.ACCEPT);
    Assertions.assertEquals(filter.getOnMismatch(), FilterReply.DENY);

    ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
    Marker marker = Mockito.mock(Marker.class);
    Mockito.when(event.getMarker()).thenReturn(marker);
    Mockito.when(marker.getName()).thenReturn("hello");
    Assertions.assertEquals(FilterReply.ACCEPT, filter.decide(event));

    Mockito.when(event.getMarker()).thenReturn(null);
    Assertions.assertEquals(FilterReply.DENY, filter.decide(event));
  }
}
