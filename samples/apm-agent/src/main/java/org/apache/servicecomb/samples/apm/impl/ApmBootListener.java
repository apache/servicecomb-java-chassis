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
package org.apache.servicecomb.samples.apm.impl;

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.event.InvocationBusinessMethodFinishEvent;
import org.apache.servicecomb.core.event.InvocationBusinessMethodStartEvent;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.InvocationStartEvent;
import org.apache.servicecomb.samples.apm.impl.output.AbstractOutputGenerator;
import org.apache.servicecomb.samples.apm.impl.output.ConsumerOutputGenerator;
import org.apache.servicecomb.samples.apm.impl.output.EdgeOutputGenerator;
import org.apache.servicecomb.samples.apm.impl.output.HeaderOutputGenerator;
import org.apache.servicecomb.samples.apm.impl.output.ProducerOutputGenerator;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

public class ApmBootListener implements BootListener {
  private AbstractOutputGenerator headerOutputGenerator = new HeaderOutputGenerator();

  private AbstractOutputGenerator consumerOutputGenerator = new ConsumerOutputGenerator();

  private AbstractOutputGenerator producerOutputGenerator = new ProducerOutputGenerator();

  private AbstractOutputGenerator edgeOutputGenerator = new EdgeOutputGenerator();

  @AllowConcurrentEvents
  @Subscribe
  public void onInvocationStart(InvocationStartEvent event) {
    ApmContext apmContext = new ApmContext();
    apmContext.setTraceId(event.getInvocation().getTraceId());
    event.getInvocation().addLocalContext("apm", apmContext);
  }

  @AllowConcurrentEvents
  @Subscribe
  public void onInvocationBusinessStart(InvocationBusinessMethodStartEvent event) {
    ApmContext apmContext = event.getInvocation().getLocalContext("apm");
    ApmContextUtils.setApmContext(apmContext);
  }

  @AllowConcurrentEvents
  @Subscribe
  public void onInvocationBusinessFinish(InvocationBusinessMethodFinishEvent event) {
    ApmContextUtils.removeApmContext();
  }

  /**
   * in real APM implementation: create span and so on for stages
   * @param event
   */
  @AllowConcurrentEvents
  @Subscribe
  public void onInvocationFinish(InvocationFinishEvent event) {
    StringBuilder sb = new StringBuilder();
    headerOutputGenerator.generate(sb, event);
    if (event.getInvocation().isConsumer()) {
      if (event.getInvocation().isEdge()) {
        edgeOutputGenerator.generate(sb, event);
      } else {
        consumerOutputGenerator.generate(sb, event);
      }
    } else {
      producerOutputGenerator.generate(sb, event);
    }
    System.out.println(sb.toString());
  }

  @Override
  public void onBootEvent(BootEvent event) {
    if (EventType.BEFORE_HANDLER.equals(event.getEventType())) {
      event.getScbEngine().getEventBus().register(this);
    }
  }
}
