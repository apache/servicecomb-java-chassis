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

package org.apache.servicecomb.metrics.core.event;

import org.apache.servicecomb.foundation.common.event.Event;
import org.apache.servicecomb.foundation.common.event.EventListener;
import org.apache.servicecomb.foundation.metrics.CounterEvent;
import org.apache.servicecomb.metrics.core.custom.CounterService;

public class CounterEventListener implements EventListener {

  private final CounterService counterService;

  public CounterEventListener(CounterService counterService) {
    this.counterService = counterService;
  }

  @Override
  public Class<? extends Event> getConcernedEvent() {
    return CounterEvent.class;
  }

  @Override
  public void process(Event data) {
    CounterEvent event = (CounterEvent) data;
    counterService.increment(event.getName(), event.getValue());
  }
}
