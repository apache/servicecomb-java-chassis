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

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.apache.servicecomb.registry.lightweight.RegisterRequest;
import org.apache.servicecomb.registry.lightweight.RegistryServerService;
import org.apache.servicecomb.registry.lightweight.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ZeroConfigServer {
  private static final Logger LOGGER = LoggerFactory.getLogger(ZeroConfigServer.class);

  private final Self self;

  private final Multicast multicast;

  private final RegistryServerService registryServerService;

  private final Map<MessageType, Consumer<?>> messageProcessors = new HashMap<>();

  private final Executor taskExecutor = Executors
      .newSingleThreadExecutor(runnable -> new Thread(runnable, "zero-config-server-task"));

  public ZeroConfigServer(Self self, Multicast multicast, RegistryServerService registryServerService) {
    this.self = self;
    this.multicast = multicast;
    this.registryServerService = registryServerService;

    addMessageProcessor(MessageType.REGISTER, this::register);
    addMessageProcessor(MessageType.UNREGISTER, registryServerService::unregister);

    Executors
        .newSingleThreadExecutor(runnable -> new Thread(runnable, "zero-config-server-recv"))
        .execute(this::recv);
  }

  private void register(RegisterRequest request) {
    if (request.isCrossApp() || Objects.equals(request.getAppId(), self.getAppId())) {
      registryServerService.register(request);
    }
  }

  private <T> void addMessageProcessor(MessageType messageType, Consumer<T> messageProcessor) {
    messageProcessors.put(messageType, messageProcessor);
  }

  private void recv() {
    for (; ; ) {
      try {
        Message<?> message = multicast.recv();
        processMessage(message);
      } catch (SocketTimeoutException e) {
        registryServerService.deleteDeadInstances(Duration.ofSeconds(90));
      } catch (Exception e) {
        LOGGER.error("failed to receive or decode message.", e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private <T> void processMessage(Message<T> message) {
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
