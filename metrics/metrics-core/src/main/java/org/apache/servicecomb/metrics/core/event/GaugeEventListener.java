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
import org.apache.servicecomb.foundation.metrics.GaugeEvent;
import org.apache.servicecomb.metrics.core.custom.GaugeService;

public class GaugeEventListener implements EventListener {

  private final GaugeService gaugeService;

  public GaugeEventListener(GaugeService gaugeService) {
    this.gaugeService = gaugeService;
  }

  @Override
  public Class<? extends Event> getConcernedEvent() {
    return GaugeEvent.class;
  }

  @Override
  public void process(Event data) {
    GaugeEvent event = (GaugeEvent) data;
    gaugeService.update(event.getName(), event.getValue());
  }
}
