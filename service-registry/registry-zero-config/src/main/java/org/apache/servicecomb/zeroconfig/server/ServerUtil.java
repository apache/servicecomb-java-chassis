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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.apache.servicecomb.zeroconfig.ZeroConfigRegistryConstants.*;

public class ServerUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerUtil.class);

  public static MulticastSocket multicastSocket;

  private static ZeroConfigRegistryService zeroConfigRegistryService;

  private static InetAddress group;

  private static ScheduledExecutorService scheduledExecutor = Executors
      .newSingleThreadScheduledExecutor();

  // 1st key: serviceId, 2nd key: instanceId
  public static Map<String, Map<String, ServerMicroserviceInstance>> microserviceInstanceMap = new ConcurrentHashMapEx<>();

  public static synchronized void init() {
    zeroConfigRegistryService = new ZeroConfigRegistryService();
    try {
      group = InetAddress.getByName(GROUP);
    } catch (UnknownHostException e) {
      LOGGER.error("Unknown host exception when creating MulticastSocket group" + e);
    }
    startEventListenerTask();
    startInstanceHealthCheckerTask();
  }

  private static void startEventListenerTask() {
    ExecutorService listenerExecutor = Executors.newSingleThreadExecutor();
    listenerExecutor.submit(() -> {
      startListenerForRegisterUnregisterEvent();
    });
  }

  private static void startInstanceHealthCheckerTask() {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        if (!microserviceInstanceMap.isEmpty()) {
          List<ServerMicroserviceInstance> unhealthyInstanceList = findUnhealthyInstances();
          if (!unhealthyInstanceList.isEmpty()) {
            removeDeadInstance(unhealthyInstanceList);
          }
        }
      }
    };
    scheduledExecutor
        .scheduleAtFixedRate(runnable, SERVER_DELAY, HEALTH_CHECK_INTERVAL, TimeUnit.SECONDS);
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

  public static ServerMicroserviceInstance convertToServerMicroserviceInstance(
      Map<String, String> serviceInstanceAttributeMap) {
    return buildServerMicroserviceInstanceFromMap(serviceInstanceAttributeMap);
  }

  private static ServerMicroserviceInstance buildServerMicroserviceInstanceFromMap(
      Map<String, String> serviceAttributeMap) {
    ServerMicroserviceInstance serverMicroserviceInstance = new ServerMicroserviceInstance();
    serverMicroserviceInstance.setInstanceId(serviceAttributeMap.get(INSTANCE_ID));
    serverMicroserviceInstance.setServiceId(serviceAttributeMap.get(SERVICE_ID));
    serverMicroserviceInstance.setStatus(serviceAttributeMap.get(STATUS));
    serverMicroserviceInstance.setHostName(serviceAttributeMap.get(HOST_NAME));
    serverMicroserviceInstance.setAppId(serviceAttributeMap.get(APP_ID));
    serverMicroserviceInstance.setServiceName(serviceAttributeMap.get(SERVICE_NAME));
    serverMicroserviceInstance.setVersion(serviceAttributeMap.get(VERSION));
    // list type attributes
    serverMicroserviceInstance
        .setEndpoints(convertStringToList(serviceAttributeMap.get(ENDPOINTS)));
    serverMicroserviceInstance.setSchemas(convertStringToList(serviceAttributeMap.get(SCHEMA_IDS)));
    return serverMicroserviceInstance;
  }

  // rest://127.0.0.1:8080$rest://127.0.0.1:8081
  // schemaId1$schemaId2
  private static List<String> convertStringToList(String listString) {
    List<String> resultList = new ArrayList<>();
    if (listString != null && !listString.isEmpty()) {
      if (listString.contains(LIST_STRING_SPLITER)) {
        resultList = Arrays.asList(listString.split("\\$"));
      } else {
        resultList.add(listString);
      }
    }
    return resultList;
  }

  private static Map<String, String> getMapFromString(String inputStr) {
    Map<String, String> map = new HashMap<>();
    String str = inputStr.substring(1, inputStr.length() - 1);
    String[] keyValue = str.split(MAP_ELEMENT_SPILITER);
    for (int i = 0; i < keyValue.length; i++) {
      String[] str2 = keyValue[i].split(MAP_KV_SPILITER);
      if (str2.length - 1 == 0) {
        map.put(str2[0].trim(), "");
      } else {
        map.put(str2[0].trim(), str2[1].trim());
      }
    }
    return map;
  }

  private static void initMulticastSocket() throws IOException {
    multicastSocket = new MulticastSocket(PORT);
    group = InetAddress.getByName(GROUP);
    multicastSocket.joinGroup(group); // need to join the group to be able to receive the data
  }

  private static void startListenerForRegisterUnregisterEvent() {
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

  private static void handleReceivedEvent(String receivedString) {
    if (receivedString.length() < 2
        || !receivedString.startsWith(MAP_STRING_LEFT) || !receivedString
        .endsWith(MAP_STRING_RIGHT)) {
      LOGGER.warn("Wrong format of the received Event string. {}", receivedString);
      return;
    }

    Map<String, String> receivedStringMap = getMapFromString(receivedString);
    String event = receivedStringMap.get(EVENT);
    if (StringUtils.isEmpty(event)) {
      LOGGER.warn("There is no Event property defined. {}", receivedStringMap);
      return;
    }

    switch (event) {
      case REGISTER_EVENT:
        LOGGER.info("Received REGISTER event{}", receivedStringMap);
        zeroConfigRegistryService.registerMicroserviceInstance(receivedStringMap);
        break;
      case UNREGISTER_EVENT:
        LOGGER.info("Received UNREGISTER event{}", receivedStringMap);
        zeroConfigRegistryService.unregisterMicroserviceInstance(receivedStringMap);
        break;
      case HEARTBEAT_EVENT:
        if (!isSelfServiceInstance(receivedStringMap)) {
          zeroConfigRegistryService.heartbeat(receivedStringMap);
        }
        break;
      default:
        LOGGER.error("Unrecognized event type. event: {}", event);
    }
  }

  private static boolean isSelfServiceInstance(Map<String, String> receivedStringMap) {
    Map<String, String> serviceInstanceMapForHeartbeat = ClientUtil
        .getServiceInstanceMapForHeartbeat();
    if (serviceInstanceMapForHeartbeat == null) {
      return false;
    } else {
      // service instance itself
      String selfServiceId = serviceInstanceMapForHeartbeat.get(SERVICE_ID);
      String selfInstanceId = serviceInstanceMapForHeartbeat.get(INSTANCE_ID);

      // received event for other service instance
      String serviceId = receivedStringMap.get(SERVICE_ID);
      String instanceId = receivedStringMap.get(INSTANCE_ID);
      return selfServiceId.equals(serviceId) && selfInstanceId.equals(instanceId);
    }

  }
}
