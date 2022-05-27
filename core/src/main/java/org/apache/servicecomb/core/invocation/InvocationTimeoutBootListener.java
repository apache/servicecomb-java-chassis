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

package org.apache.servicecomb.core.invocation;

import java.util.List;

import org.apache.servicecomb.core.event.InvocationBusinessFinishEvent;
import org.apache.servicecomb.core.event.InvocationBusinessMethodStartEvent;
import org.apache.servicecomb.core.event.InvocationHandlersStartEvent;
import org.apache.servicecomb.core.event.InvocationRunInExecutorStartEvent;
import org.apache.servicecomb.core.event.InvocationStartEvent;
import org.apache.servicecomb.core.event.InvocationStartSendRequestEvent;
import org.apache.servicecomb.core.event.InvocationTimeoutCheckEvent;
import org.apache.servicecomb.core.invocation.timeout.PassingTimeStrategy;
import org.apache.servicecomb.foundation.common.event.EnableExceptionPropagation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings({"UnstableApiUsage", "unused"})
@Component
public class InvocationTimeoutBootListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(InvocationTimeoutBootListener.class);

  public static final String PREFIX = "servicecomb.invocation.timeout.check";

  public static final String STRATEGY = PREFIX + ".strategy";

  public static final String ENABLED = PREFIX + ".enabled";

  private final InvocationTimeoutStrategy strategy;

  public InvocationTimeoutBootListener(EventBus eventBus, List<InvocationTimeoutStrategy> strategies,
      Environment environment) {
    if (!environment.getProperty(ENABLED, boolean.class, false)) {
      strategy = null;
      return;
    }

    String strategyName = environment.getProperty(STRATEGY, PassingTimeStrategy.NAME);
    // if strategyName is wrong, then just throw exception
    strategy = strategies.stream()
        .filter(invocationTimeoutStrategy -> strategyName.equals(invocationTimeoutStrategy.name()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("can not find InvocationTimeoutStrategy, name=" + strategyName));
    eventBus.register(this);
  }

  @Subscribe
  @EnableExceptionPropagation
  public void onInvocationTimeoutCheckEvent(InvocationTimeoutCheckEvent event) {
    strategy.checkTimeout(event.getInvocation());
  }

  @Subscribe
  public void onInvocationStartEvent(InvocationStartEvent event) {
    strategy.start(event.getInvocation());
  }

  @Subscribe
  @EnableExceptionPropagation
  public void onInvocationRunInExecutorStartEvent(InvocationRunInExecutorStartEvent event) {
    strategy.startRunInExecutor(event.getInvocation());
  }

  @Subscribe
  @EnableExceptionPropagation
  public void onInvocationHandlersStartEvent(InvocationHandlersStartEvent event) {
    strategy.startHandlers(event.getInvocation());
  }

  @Subscribe
  @EnableExceptionPropagation
  public void onInvocationBusinessMethodStartEvent(InvocationBusinessMethodStartEvent event) {
    strategy.startBusinessMethod(event.getInvocation());
  }

  @Subscribe
  @EnableExceptionPropagation
  public void onInvocationBusinessFinishEvent(InvocationBusinessFinishEvent event) {
    strategy.finishBusinessMethod(event.getInvocation());
  }

  @Subscribe
  @EnableExceptionPropagation
  public void onInvocationStartSendRequestEvent(InvocationStartSendRequestEvent event) {
    strategy.beforeSendRequest(event.getInvocation());
  }
}
