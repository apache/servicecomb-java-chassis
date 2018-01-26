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
import org.apache.servicecomb.foundation.metrics.WindowCounterEvent;
import org.apache.servicecomb.metrics.core.custom.WindowCounterService;

public class WindowCounterEventListener implements EventListener {

  private final WindowCounterService windowCounterService;

  public WindowCounterEventListener(WindowCounterService windowCounterService) {
    this.windowCounterService = windowCounterService;
  }

  @Override
  public Class<? extends Event> getConcernedEvent() {
    return WindowCounterEvent.class;
  }

  @Override
  public void process(Event data) {
    WindowCounterEvent event = (WindowCounterEvent) data;
    windowCounterService.record(event.getName(), event.getValue());
  }
}
