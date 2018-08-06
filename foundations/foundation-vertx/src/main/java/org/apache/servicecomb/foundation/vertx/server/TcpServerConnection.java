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
package org.apache.servicecomb.foundation.vertx.server;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.vertx.ClientEvent;
import org.apache.servicecomb.foundation.vertx.ConnectionEvent;
import org.apache.servicecomb.foundation.vertx.TransportType;
import org.apache.servicecomb.foundation.vertx.tcp.TcpConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.net.NetSocket;
import io.vertx.core.net.impl.NetSocketImpl;

public class TcpServerConnection extends TcpConnection {
  private static final Logger LOGGER = LoggerFactory.getLogger(TcpServerConnection.class);

  protected TcpParser splitter;

  public void init(NetSocket netSocket, AtomicInteger connectedCounter) {
    // currently, socket always be NetSocketImpl
    this.initNetSocket((NetSocketImpl) netSocket);

    String remoteAddress = netSocket.remoteAddress().toString();
    LOGGER.info("connect from {}, in thread {}",
        remoteAddress,
        Thread.currentThread().getName());
    netSocket.exceptionHandler(e -> {
      LOGGER.error("disconected from {}, in thread {}, cause {}",
          remoteAddress,
          Thread.currentThread().getName(),
          e.getMessage());
    });
    netSocket.closeHandler(Void -> {
      LOGGER.error("disconected from {}, in thread {}",
          remoteAddress,
          Thread.currentThread().getName());

      int connectedCount = connectedCounter.decrementAndGet();
      EventManager.post(new ClientEvent(remoteAddress, ConnectionEvent.Closed, TransportType.Highway, connectedCount));
    });

    netSocket.handler(splitter);
  }
}
