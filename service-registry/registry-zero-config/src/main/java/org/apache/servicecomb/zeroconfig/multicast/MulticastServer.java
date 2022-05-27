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

package org.apache.servicecomb.zeroconfig.multicast;

import java.net.SocketTimeoutException;
import java.util.concurrent.Executors;

import org.apache.servicecomb.registry.lightweight.Message;
import org.apache.servicecomb.registry.lightweight.MessageExecutor;
import org.apache.servicecomb.zeroconfig.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
//@Conditional(ConditionOnMulticast.class)
@SuppressWarnings("unused")
public class MulticastServer {
  private static final Logger LOGGER = LoggerFactory.getLogger(MulticastServer.class);

  private final Multicast multicast;

  private final MessageExecutor messageExecutor;

  public MulticastServer(Config config, Multicast multicast, MessageExecutor messageExecutor) {
    this.multicast = multicast;
    this.messageExecutor = messageExecutor;

    // delete after support @Conditional
    if (!config.isMulticast()) {
      return;
    }

    Executors
        .newSingleThreadExecutor(runnable -> new Thread(runnable, "multicast-server-recv"))
        .execute(this::recv);
    messageExecutor.startCheckDeadInstances(config.getCheckDeadInstancesInterval());
  }

  @SuppressWarnings("InfiniteLoopStatement")
  private void recv() {
    for (; ; ) {
      Message<?> message = recvMsg();
      if (message == null) {
        continue;
      }

      messageExecutor.processMessage(message);
    }
  }

  private Message<?> recvMsg() {
    try {
      return multicast.recv();
    } catch (SocketTimeoutException ignore) {
      return null;
    } catch (Exception e) {
      LOGGER.error("failed to receive or decode message.", e);
      return null;
    }
  }
}
