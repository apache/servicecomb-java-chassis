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

import org.slf4j.Marker;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Create a marker filter for logback usage. lockback 1.2.3 do not provide any marker filter. But higher
 * versions provide one. If you are using a more higher version, you can use the native one.
 *
 * see: https://github.com/qos-ch/logback/pull/407/files
 */
public class MarkerFilter extends AbstractMatcherFilter<ILoggingEvent> {
  private String marker;

  public MarkerFilter() {
    setOnMatch(FilterReply.ACCEPT);
    setOnMismatch(FilterReply.DENY);
  }

  @Override
  public FilterReply decide(ILoggingEvent event) {
    if (!isStarted()) {
      return FilterReply.NEUTRAL;
    }

    Marker currentMarker = event.getMarker();

    if (currentMarker == null || !marker.equals(currentMarker.getName())) {
      return onMismatch;
    } else {
      return onMatch;
    }
  }

  public void setMarker(String marker) {
    this.marker = marker;
  }

  @Override
  public void start() {
    if (this.marker != null) {
      super.start();
    }
  }
}
