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
package org.apache.servicecomb.it;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.SCBStatus;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersionRule;
import org.apache.servicecomb.serviceregistry.task.event.PeriodicPullEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ITUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ITUtils.class);

  private ITUtils() {
  }

  public static void forceWait(TimeUnit timeUnit, long timeout) {
    try {
      timeUnit.sleep(timeout);
    } catch (InterruptedException e) {
      // eat the exception
    }
  }

  public static void waitBootFinished() {
    for (; ; ) {
      if (SCBEngine.getInstance().getStatus().equals(SCBStatus.UP)) {
        return;
      }

      forceWait(TimeUnit.MILLISECONDS, 100);
    }
  }

  public static Map<String, MicroserviceInstance> waitMicroserviceReadyAndLimit(String appId, String microserviceName,
      String strVersionRule, int minInstanceCount) {
    Map<String, MicroserviceInstance> instances = waitMicroserviceReady(appId,
        microserviceName,
        strVersionRule,
        minInstanceCount);
    return instances.values()
        .stream()
        .sorted(Comparator.comparing(MicroserviceInstance::getInstanceId))
        .limit(minInstanceCount)
        .collect(Collectors.toMap(MicroserviceInstance::getInstanceId, Function.identity()));
  }

  public static Map<String, MicroserviceInstance> waitMicroserviceReady(String appId, String microserviceName,
      String strVersionRule, int minInstanceCount) {
    LOGGER.info("waiting for microservice online. appId={}, name={}, minInstanceCount={}",
        appId,
        microserviceName,
        minInstanceCount);

    Map<String, MicroserviceInstance> instances;
    for (;;) {
      MicroserviceVersionRule microserviceVersionRule = RegistryUtils.getServiceRegistry()
          .getAppManager()
          .getOrCreateMicroserviceVersionRule(appId, microserviceName, strVersionRule);
      instances = microserviceVersionRule.getInstances();
      if (instances.size() >= minInstanceCount) {
        break;
      }

      LOGGER.info(
          "waiting for microservice online. appId={}, name={}, expect minInstanceCount={}, real instanceCount={}.",
          appId,
          microserviceName,
          minInstanceCount,
          instances.size());
      // pull at once
      EventManager.getEventBus().post(new PeriodicPullEvent());
      forceWait(TimeUnit.SECONDS, 1);
    }

    LOGGER.info("microservice already online. appId={}, name={}, instanceCount={}",
        appId,
        microserviceName,
        minInstanceCount);
    return instances;
  }

  public static void invokeExactStaticMethod(final Class<?>[] classes, final String methodName, Object... args) {
    for (Class<?> cls : classes) {
      invokeExactStaticMethod(cls, methodName, args);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T invokeExactStaticMethod(final Class<?> cls, final String methodName, Object... args) {
    try {
      return (T) MethodUtils.invokeExactStaticMethod(cls, methodName, args);
    } catch (Throwable e) {
      throw new IllegalStateException(String.format("Failed to invoke, class=%s, method=%s", cls.getName(), methodName),
          e);
    }
  }
}
