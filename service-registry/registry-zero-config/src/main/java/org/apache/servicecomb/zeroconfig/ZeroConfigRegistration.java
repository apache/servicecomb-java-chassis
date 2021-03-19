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
package org.apache.servicecomb.zeroconfig;

import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.ORDER;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.registry.api.event.MicroserviceInstanceRegisteredEvent;
import org.apache.servicecomb.registry.lightweight.AbstractLightweightRegistration;
import org.apache.servicecomb.registry.lightweight.RegisterInstanceEvent;
import org.apache.servicecomb.registry.lightweight.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@Component
public class ZeroConfigRegistration extends AbstractLightweightRegistration {
  private static final Logger LOGGER = LoggerFactory.getLogger(ZeroConfigRegistration.class);

  private static final String NAME = "zero-config registration";

  private final Config config;

  private final Multicast multicast;

  private final Self self;

  private final EventBus eventBus;

  private final ScheduledExecutorService executorService = Executors
      .newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, "zero-config-register"));

  public ZeroConfigRegistration(Config config, Multicast multicast, Self self, EventBus eventBus) {
    this.config = config;
    this.multicast = multicast;
    this.self = self;
    this.eventBus = eventBus;

    eventBus.register(this);
  }

  private void sendRegister() {
    try {
      multicast.send(MessageType.REGISTER, self.buildRegisterRequest());
    } catch (Exception e) {
      LOGGER.error("register failed.", e);
    }
  }

  @Subscribe
  public void onRegisterInstance(RegisterInstanceEvent event) {
    if (event.getInstance().getInstanceId().equals(self.getInstance().getInstanceId())) {
      return;
    }

    sendRegister();
  }

  @Override
  public boolean enabled() {
    return config.isEnabled();
  }

  @Override
  public void init() {

  }

  @Override
  public void run() {
    // switch to registered status, before send register message
    // otherwise send message maybe failed
    eventBus.post(new MicroserviceInstanceRegisteredEvent(
        NAME,
        self.getInstance().getInstanceId(),
        false
    ));
    executorService.scheduleAtFixedRate(this::sendRegister, 0, 10, TimeUnit.SECONDS);
  }

  @Override
  public void destroy() {
    try {
      multicast.send(MessageType.UNREGISTER, self.buildUnregisterRequest());
    } catch (Exception e) {
      LOGGER.error("unregister failed.", e);
    }
  }

  @Override
  public int getOrder() {
    return ORDER;
  }

  @Override
  public String name() {
    return NAME;
  }
}
