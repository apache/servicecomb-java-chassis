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
package org.apache.servicecomb.zeroconfig.client;

import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.zeroconfig.server.ServerMicroserviceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.apache.servicecomb.zeroconfig.ZeroConfigRegistryConstants.*;

public class ClientUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientUtil.class);

  private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  private static Microservice microserviceSelf = new Microservice();

  private static Map<String, String> serviceInstanceMapForHeartbeat = null;

  private static MulticastSocket multicastSocket;

  public static Microservice getMicroserviceSelf() {
    return microserviceSelf;
  }

  public static void setMicroserviceSelf(Microservice microserviceSelf) {
    ClientUtil.microserviceSelf = microserviceSelf;
  }

  public static Map<String, String> getServiceInstanceMapForHeartbeat() {
    return serviceInstanceMapForHeartbeat;
  }

  public static void setServiceInstanceMapForHeartbeat(
      Map<String, String> serviceInstanceMapForHeartbeat) {
    ClientUtil.serviceInstanceMapForHeartbeat = serviceInstanceMapForHeartbeat;
  }

  public static synchronized void init() {
    try {
      multicastSocket = new MulticastSocket();
      multicastSocket.setLoopbackMode(false);
      multicastSocket.setTimeToLive(TIME_TO_LIVE);
    } catch (IOException e) {
      LOGGER.error("Failed to create MulticastSocket object", e);
    }

    Runnable heartbeatRunnable = new Runnable() {
      @Override
      public void run() {
        if (serviceInstanceMapForHeartbeat != null && !serviceInstanceMapForHeartbeat.isEmpty()) {
          // after first registration succeeds
          try {
            byte[] heartbeatEventDataBytes = serviceInstanceMapForHeartbeat.toString().getBytes();
            DatagramPacket instanceDataPacket = new DatagramPacket(heartbeatEventDataBytes,
                heartbeatEventDataBytes.length,
                InetAddress.getByName(GROUP), PORT);

            multicastSocket.send(instanceDataPacket);
          } catch (Exception e) {
            LOGGER.error("Failed to send heartbeat event for object: {}",
                serviceInstanceMapForHeartbeat, e);
          }
        }
      }
    };
    executor.scheduleAtFixedRate(heartbeatRunnable, CLIENT_DELAY, HEALTH_CHECK_INTERVAL,
        TimeUnit.SECONDS);
  }

  public static Map<String, String> convertToRegisterDataModel(String serviceId,
      String microserviceInstanceId,
      MicroserviceInstance microserviceInstance, Microservice microservice) {
    Map<String, String> serviceInstanceTextAttributesMap = new HashMap<>();

    serviceInstanceTextAttributesMap.put(EVENT, REGISTER_EVENT);
    serviceInstanceTextAttributesMap.put(VERSION, microservice.getVersion());
    serviceInstanceTextAttributesMap.put(SERVICE_ID, serviceId);
    serviceInstanceTextAttributesMap.put(INSTANCE_ID, microserviceInstanceId);
    serviceInstanceTextAttributesMap.put(STATUS, microserviceInstance.getStatus().toString());
    serviceInstanceTextAttributesMap.put(APP_ID, microservice.getAppId());
    serviceInstanceTextAttributesMap.put(SERVICE_NAME, microservice.getServiceName());

    String hostName = microserviceInstance.getHostName();
    serviceInstanceTextAttributesMap.put(HOST_NAME, hostName);

    // schema1$schema2
    serviceInstanceTextAttributesMap
        .put(ENDPOINTS, String.join(LIST_STRING_SPLITER, microserviceInstance.getEndpoints()));
    serviceInstanceTextAttributesMap
        .put(SCHEMA_IDS, String.join(LIST_STRING_SPLITER, microservice.getSchemas()));

    return serviceInstanceTextAttributesMap;
  }

  public static MicroserviceInstance convertToClientMicroserviceInstance(
      ServerMicroserviceInstance serverMicroserviceInstance) {
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    microserviceInstance.setServiceId(serverMicroserviceInstance.getServiceId());
    microserviceInstance.setInstanceId(serverMicroserviceInstance.getInstanceId());
    microserviceInstance.setHostName(serverMicroserviceInstance.getHostName());
    microserviceInstance.setEndpoints(serverMicroserviceInstance.getEndpoints());
    microserviceInstance
        .setStatus(MicroserviceInstanceStatus.valueOf(serverMicroserviceInstance.getStatus()));
    return microserviceInstance;
  }

  public static Microservice convertToClientMicroservice(
      ServerMicroserviceInstance serverMicroserviceInstance) {
    Microservice microservice = new Microservice();
    microservice.setAppId(serverMicroserviceInstance.getAppId());
    microservice.setServiceId(serverMicroserviceInstance.getServiceId());
    microservice.setServiceName(serverMicroserviceInstance.getServiceName());
    microservice.setVersion(serverMicroserviceInstance.getVersion());
    microservice.setStatus(serverMicroserviceInstance.getStatus());
    microservice.setSchemas(serverMicroserviceInstance.getSchemas());
    return microservice;
  }

  public static String generateServiceId(Microservice microservice) {
    String serviceIdStringIndex = String.join(SERVICE_ID_SPLITER, microservice.getAppId(),
        microservice.getServiceName(), microservice.getVersion());
    return UUID.nameUUIDFromBytes(serviceIdStringIndex.getBytes()).toString()
        .split(UUID_SPLITER)[0];
  }

  public static String generateServiceInstanceId() {
    return UUID.randomUUID().toString().split(UUID_SPLITER)[0];
  }

}
