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

import java.io.IOException;

import org.apache.servicecomb.registry.lightweight.MessageType;
import org.apache.servicecomb.registry.lightweight.RegisterInstanceEvent;
import org.apache.servicecomb.zeroconfig.multicast.Multicast;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.eventbus.Subscribe;

@SuppressWarnings("UnstableApiUsage")
public class ZeroConfigRegistration extends AbstractZeroConfigRegistration<ZeroConfigRegistrationInstance> implements
    InitializingBean {
  protected Multicast multicast;

  @Autowired
  public ZeroConfigRegistration setMulticast(Multicast multicast) {
    this.multicast = multicast;
    return this;
  }

  @Override
  public String name() {
    return ZeroConfigConst.ZERO_CONFIG_REGISTRY_NAME;
  }

  // delete after support @Conditional
  @Override
  public boolean enabled() {
    return true;
  }

  @Override
  public void afterPropertiesSet() {
    eventBus.register(this);
  }

  @Override
  protected void doSendRegister() throws IOException {
    multicast.send(MessageType.REGISTER, self.buildRegisterRequest());
  }

  @Override
  protected void doSendUnregister() throws IOException {
    multicast.send(MessageType.UNREGISTER, self.buildUnregisterRequest());
  }

  @Override
  public ZeroConfigRegistrationInstance getMicroserviceInstance() {
    return new ZeroConfigRegistrationInstance(self);
  }

  @SuppressWarnings("unused")
  @Subscribe
  public void onRegisterInstance(RegisterInstanceEvent event) {
    if (event.getInstance().getInstanceId().equals(self.getInstance().getInstanceId())) {
      return;
    }

    sendRegister();
  }
}
