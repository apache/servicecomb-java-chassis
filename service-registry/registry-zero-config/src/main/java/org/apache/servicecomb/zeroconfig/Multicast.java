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
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.net.HostAndPort;
import com.netflix.config.DynamicPropertyFactory;

import io.vertx.core.json.jackson.DatabindCodec;

@Component
public class Multicast {
  private static final Logger LOGGER = LoggerFactory.getLogger(Multicast.class);

  private final InetSocketAddress bindAddress;

  // (224.0.0.0, 239.255.255.255]
  private final InetAddress group;

  private final MulticastSocket multicastSocket;

  private final byte[] recvBuffer = new byte[ZeroConfigConst.MAX_PACKET_SIZE];

  private final DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);

  public Multicast() throws IOException {
    this.bindAddress = initBindAddress();
    this.group = initGroup();
    LOGGER.info("zero config, address: {}", bindAddress);
    LOGGER.info("zero config, group: {}", group);

    this.multicastSocket = new MulticastSocket(bindAddress);
    this.multicastSocket.joinGroup(group);
    this.multicastSocket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(5));
  }

  private InetSocketAddress initBindAddress() {
    HostAndPort hostAndPort = HostAndPort
        .fromString(readString(ZeroConfigConst.CFG_ADDRESS, ZeroConfigConst.DEFAULT_ADDRESS));
    return new InetSocketAddress(hostAndPort.getHost(), hostAndPort.getPort());
  }

  private InetAddress initGroup() throws UnknownHostException {
    return InetAddress.getByName(readString(ZeroConfigConst.CFG_GROUP, ZeroConfigConst.DEFAULT_GROUP));
  }

  private String readString(String key, String defaultValue) {
    return DynamicPropertyFactory.getInstance().getStringProperty(key, defaultValue).get();
  }

  public <T> void send(MessageType type, T body) throws IOException {
    Message<T> message = Message.of(type, body);
    byte[] buffer = DatabindCodec.mapper().writeValueAsBytes(message);
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, bindAddress.getPort());
    multicastSocket.send(packet);
  }

  public Message<?> recv() throws IOException {
    multicastSocket.receive(recvPacket);

    return DatabindCodec.mapper()
        .readValue(recvPacket.getData(), 0, recvPacket.getLength(), Message.class);
  }
}
