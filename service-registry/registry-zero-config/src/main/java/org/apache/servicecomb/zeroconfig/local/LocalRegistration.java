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

package org.apache.servicecomb.zeroconfig.local;

import org.apache.servicecomb.registry.lightweight.Message;
import org.apache.servicecomb.registry.lightweight.MessageExecutor;
import org.apache.servicecomb.registry.lightweight.MessageType;
import org.apache.servicecomb.registry.lightweight.RegisterRequest;
import org.apache.servicecomb.registry.lightweight.UnregisterRequest;
import org.apache.servicecomb.zeroconfig.AbstractZeroConfigRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * for single node environments
 */
@Component
//@Conditional(ConditionOnLocal.class)
public class LocalRegistration extends AbstractZeroConfigRegistration {
  private static final String NAME = "zero-config-local";

  private MessageExecutor messageExecutor;

  @Autowired
  public LocalRegistration setMessageExecutor(MessageExecutor messageExecutor) {
    this.messageExecutor = messageExecutor;
    return this;
  }

  @Override
  public String name() {
    return NAME;
  }

  // delete after support @Conditional
  @Override
  public boolean enabled() {
    return config.isLocal();
  }

  @Override
  protected void doSendRegister() {
    Message<RegisterRequest> message = Message.of(MessageType.REGISTER, self.buildRegisterRequest());
    messageExecutor.processMessage(message);
  }

  @Override
  protected void doSendUnregister() {
    Message<UnregisterRequest> message = Message.of(MessageType.UNREGISTER, self.buildUnregisterRequest());
    messageExecutor.processMessage(message);
  }
}
