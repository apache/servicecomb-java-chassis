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

import static javax.ws.rs.core.Response.Status.REQUEST_TIMEOUT;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationBusinessFinishEvent;
import org.apache.servicecomb.core.event.InvocationBusinessMethodStartEvent;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.InvocationHandlersStartEvent;
import org.apache.servicecomb.core.event.InvocationRunInExecutorStartEvent;
import org.apache.servicecomb.core.event.InvocationStartEvent;
import org.apache.servicecomb.core.event.InvocationStartSendRequestEvent;
import org.apache.servicecomb.core.event.InvocationTimeoutCheckEvent;
import org.apache.servicecomb.core.exception.ExceptionCodes;
import org.apache.servicecomb.foundation.common.event.EnableExceptionPropagation;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.Subscribe;
import com.netflix.config.DynamicPropertyFactory;

@Component
public class InvocationTimeoutBootListener implements BootListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(InvocationTimeoutBootListener.class);
  
  public static final String ENABLE_TIMEOUT_CHECK = "servicecomb.invocation.enableTimeoutCheck";

  public static boolean timeoutCheckEnabled() {
    return DynamicPropertyFactory.getInstance().getBooleanProperty
        (ENABLE_TIMEOUT_CHECK, true).get();
  }

  @Override
  public void onAfterRegistry(BootEvent event) {
    if (timeoutCheckEnabled()) {
      EventManager.getEventBus().register(this);
    }
  }

  @Subscribe
  public void onInvocationStartEvent(InvocationStartEvent event) {
    Invocation invocation = event.getInvocation();

    // not initialized
    // 1. when first time received request
    // 2. when first time send request not a user thread
    // initialized
    // 1. send request in the progress of processing request
    if (invocation.getLocalContext(Const.CONTEXT_TIME_CURRENT) == null) {
      invocation.addLocalContext(Const.CONTEXT_TIME_CURRENT, invocation.getInvocationStageTrace().getStart());
    }

    if (invocation.getLocalContext(Const.CONTEXT_TIME_ELAPSED) == null) {
      String elapsed = invocation.getContext(Const.CONTEXT_TIME_ELAPSED);
      if (StringUtils.isEmpty(elapsed)) {
        invocation.addLocalContext(Const.CONTEXT_TIME_ELAPSED, 0L);
        return;
      }

      try {
        invocation.addLocalContext(Const.CONTEXT_TIME_ELAPSED, Long.parseLong(elapsed));
      } catch (NumberFormatException e) {
        LOGGER.error("Not expected number format exception, attacker?");
        invocation.addLocalContext(Const.CONTEXT_TIME_ELAPSED, 0L);
      }
    }
  }

  @Subscribe
  @EnableExceptionPropagation
  public void onInvocationTimeoutCheckEvent(InvocationTimeoutCheckEvent event) {
    ensureInvocationNotTimeout(event.getInvocation());
  }

  /**
   * check if invocation is timeout.
   *
   * @throws InvocationException if timeout, throw an exception. Will not throw exception twice if this method called
   *  after timeout.
   */
  private void ensureInvocationNotTimeout(Invocation invocation) {
    if (invocation.getOperationMeta().getConfig().getNanoInvocationTimeout() > 0 && calculateElapsedTime(invocation) >
        invocation.getOperationMeta().getConfig().getNanoInvocationTimeout()) {
      if (invocation.getLocalContext(Const.CONTEXT_TIMED_OUT) != null) {
        // already timed out, do not throw exception again
        return;
      }
      invocation.addLocalContext(Const.CONTEXT_TIMED_OUT, true);
      throw new InvocationException(REQUEST_TIMEOUT,
          ExceptionCodes.INVOCATION_TIMEOUT, "Invocation Timeout.");
    }
  }

  private long calculateElapsedTime(Invocation invocation) {
    return System.nanoTime() - (long) invocation.getLocalContext(Const.CONTEXT_TIME_CURRENT)
        + (long) invocation.getLocalContext(Const.CONTEXT_TIME_ELAPSED);
  }

  @Subscribe
  @EnableExceptionPropagation
  public void onInvocationRunInExecutorStartEvent(InvocationRunInExecutorStartEvent event) {
    ensureInvocationNotTimeout(event.getInvocation());
  }

  @Subscribe
  @EnableExceptionPropagation
  public void onInvocationHandlersStartEvent(InvocationHandlersStartEvent event) {
    ensureInvocationNotTimeout(event.getInvocation());
  }

  @Subscribe
  @EnableExceptionPropagation
  public void onInvocationBusinessMethodStartEvent(InvocationBusinessMethodStartEvent event) {
    ensureInvocationNotTimeout(event.getInvocation());
  }

  @Subscribe
  @EnableExceptionPropagation
  public void onInvocationBusinessFinishEvent(InvocationBusinessFinishEvent event) {
    ensureInvocationNotTimeout(event.getInvocation());
  }

  @Subscribe
  @EnableExceptionPropagation
  public void onInvocationStartSendRequestEvent(InvocationStartSendRequestEvent event) {
    Invocation invocation = event.getInvocation();
    ensureInvocationNotTimeout(invocation);
    invocation.addContext(Const.CONTEXT_TIME_ELAPSED, Long.toString(calculateElapsedTime(invocation)));
  }

  @Subscribe
  @EnableExceptionPropagation
  public void onInvocationFinishEvent(InvocationFinishEvent event) {
    ensureInvocationNotTimeout(event.getInvocation());
  }
}
