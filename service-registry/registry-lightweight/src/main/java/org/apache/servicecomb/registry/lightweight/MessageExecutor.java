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

package org.apache.servicecomb.registry.lightweight;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

@Component
public class MessageExecutor {
  private final Self self;

  private final StoreService storeService;

  private final Map<MessageType, Consumer<?>> messageProcessors = new HashMap<>();

  private final ScheduledExecutorService taskExecutor = Executors
      .newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, "lightweight-message-executor"));

  public MessageExecutor(Self self, StoreService storeService) {
    this.self = self;
    this.storeService = storeService;

    addMessageProcessor(MessageType.REGISTER, this::register);
    addMessageProcessor(MessageType.UNREGISTER, storeService::unregister);
  }

  public void startCheckDeadInstances(Duration interval) {
    taskExecutor.scheduleAtFixedRate(
        () -> storeService.deleteDeadInstances(interval),
        0, interval.getSeconds(), TimeUnit.SECONDS);
  }

  private void register(RegisterRequest request) {
    if (request.isCrossApp() || Objects.equals(request.getAppId(), self.getAppId())) {
      storeService.register(request);
    }
  }

  private <T> void addMessageProcessor(MessageType messageType, Consumer<T> messageProcessor) {
    messageProcessors.put(messageType, messageProcessor);
  }

  @SuppressWarnings("unchecked")
  public <T> void processMessage(Message<T> message) {
    Consumer<T> consumer = (Consumer<T>) messageProcessors.get(message.getType());

    taskExecutor.execute(() -> {
      try {
        consumer.accept(message.getBody());
      } catch (Exception ignore) {
        // already log inside message processor
      }
    });
  }
}
