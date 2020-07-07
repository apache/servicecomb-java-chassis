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
package org.apache.servicecomb.zeroconfig.server;

import io.vertx.core.json.Json;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.zeroconfig.client.ClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.apache.servicecomb.zeroconfig.ZeroConfigRegistryConstants.*;

public class ServerUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerUtil.class);

  public static final ServerUtil INSTANCE = new ServerUtil();

  public  MulticastSocket multicastSocket;
  private ZeroConfigRegistryService zeroConfigRegistryService;
  private InetAddress group;

  // 1st key: serviceId, 2nd key: instanceId
  public static Map<String, Map<String, ServerMicroserviceInstance>> microserviceInstanceMap = new ConcurrentHashMapEx<>();

  private ServerUtil(){}

  public synchronized void init() {
    this.zeroConfigRegistryService = new ZeroConfigRegistryService();
    try {
      this.group = InetAddress.getByName(GROUP);
    } catch (UnknownHostException e) {
      LOGGER.error("Unknown host exception when creating MulticastSocket group" + e);
    }
    Executors.newSingleThreadExecutor().submit(() -> {
      startListenerForRegisterUnregisterEvent();
    });
    Executors.newSingleThreadScheduledExecutor()
        .scheduleAtFixedRate(this::runInstanceHealthCheckerTask, SERVER_DELAY, HEALTH_CHECK_INTERVAL, TimeUnit.SECONDS);
  }

  private void runInstanceHealthCheckerTask() {
    if (!microserviceInstanceMap.isEmpty()) {
      List<ServerMicroserviceInstance> unhealthyInstanceList = findUnhealthyInstances();
      if (!unhealthyInstanceList.isEmpty()) {
        removeDeadInstance(unhealthyInstanceList);
      }
    }
  }

  private static List<ServerMicroserviceInstance> findUnhealthyInstances() {
    List<ServerMicroserviceInstance> unhealthyInstanceList = new ArrayList<>();
    microserviceInstanceMap.forEach((serviceId, instanceIdMap) -> {
      instanceIdMap.forEach((instanceId, instance) -> {
        // current time - last heartbeattime > 3 seconds => dead instance (no heartbeat for more than 3 seconds)
        if (instance.getLastHeartbeatTimeStamp() != null &&
            instance.getLastHeartbeatTimeStamp().plusSeconds(HEALTH_CHECK_INTERVAL)
                .compareTo(Instant.now()) < 0) {
          unhealthyInstanceList.add(instance);
          LOGGER.info("Detected a unhealthy service instance. serviceId: {}, instanceId: {}",
              instance.getServiceId(), instance.getInstanceId());
        }
      });
    });
    return unhealthyInstanceList;
  }

  private static void removeDeadInstance(List<ServerMicroserviceInstance> unhealthyInstanceList) {
    for (ServerMicroserviceInstance deadInstance : unhealthyInstanceList) {
      microserviceInstanceMap
          .computeIfPresent(deadInstance.getServiceId(), (serviceId, instanceIdMap) -> {
            instanceIdMap.computeIfPresent(deadInstance.getInstanceId(), (instanceId, instance) -> {
              // remove this instance from inner instance map
              return null;
            });
            // if the inner instance map is empty, remove the service itself from the outer map too
            return !instanceIdMap.isEmpty() ? instanceIdMap : null;
          });
    }
  }

  private void initMulticastSocket() throws IOException {
    this.multicastSocket = new MulticastSocket(PORT);
    this.multicastSocket.joinGroup(this.group); // need to join the group to be able to receive the data
  }

  private void startListenerForRegisterUnregisterEvent() {
    try {
      byte[] buffer = new byte[DATA_PACKET_BUFFER_SIZE];
      initMulticastSocket();
      while (true) {
        DatagramPacket receivePacketBuffer = new DatagramPacket(buffer, buffer.length);
        try {
          multicastSocket.receive(receivePacketBuffer);
        } catch (Throwable t) {
          LOGGER.error("Caught error when receiving the data packet", t.getMessage());
          if (multicastSocket.isClosed()) {
            LOGGER.info("MulticastSocket is closed. Going to restart it.");
            initMulticastSocket();
          }
          continue;
        }

        String receivedEventString = new String(receivePacketBuffer.getData(), 0,
            receivePacketBuffer.getLength(), ENCODE).trim();
        handleReceivedEvent(receivedEventString);
      }
    } catch (IOException e) {
      throw new IllegalStateException(
          "Failed to create MulticastSocket object. Zero-Config init failed! ", e);
    } finally {
      if (multicastSocket != null) {
        try {
          multicastSocket.leaveGroup(group);
          multicastSocket.close();
        } catch (IOException e1) {
          //  如果没有加入group不会报错，但是如果group不是组播地址将报错
          LOGGER.error("Failed to close the MulticastSocket" + e1);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void handleReceivedEvent(String receivedString) {
    if (receivedString.length() < 2
        || !receivedString.startsWith(MAP_STRING_LEFT) || !receivedString
        .endsWith(MAP_STRING_RIGHT)) {
      LOGGER.warn("Wrong format of the received Event string. {}", receivedString);
      return;
    }

    ServerMicroserviceInstance receivedInstance = Json.decodeValue(receivedString, ServerMicroserviceInstance.class);
    String event = receivedInstance.getEvent();
    if (StringUtils.isEmpty(event)) {
      LOGGER.warn("There is no Event property defined. {}", receivedInstance);
      return;
    }

    switch (event) {
      case REGISTER_EVENT:
        LOGGER.info("Received REGISTER event: {}", receivedInstance);
        zeroConfigRegistryService.registerMicroserviceInstance(receivedInstance);
        break;
      case UNREGISTER_EVENT:
        LOGGER.info("Received UNREGISTER event: {}", receivedInstance);
        zeroConfigRegistryService.unregisterMicroserviceInstance(receivedInstance);
        break;
      case HEARTBEAT_EVENT:
        if (!this.isSelfServiceInstance(receivedInstance)) {
          zeroConfigRegistryService.heartbeat(receivedInstance);
        }
        break;
      default:
        LOGGER.error("Unrecognized event type. event: {}", event);
    }
  }

  private boolean isSelfServiceInstance(ServerMicroserviceInstance receivedInstance) {
    ServerMicroserviceInstance serviceInstanceForHeartbeat = ClientUtil.INSTANCE.getServiceInstanceForHeartbeat();
    if (serviceInstanceForHeartbeat == null) {
      return false;
    } else {
      // service instance itself
      String selfServiceId = serviceInstanceForHeartbeat.getServiceId();
      String selfInstanceId = serviceInstanceForHeartbeat.getInstanceId();

      // received event for other service instance
      String serviceId = receivedInstance.getServiceId();
      String instanceId = receivedInstance.getInstanceId();
      return selfServiceId.equals(serviceId) && selfInstanceId.equals(instanceId);
    }

  }
}
