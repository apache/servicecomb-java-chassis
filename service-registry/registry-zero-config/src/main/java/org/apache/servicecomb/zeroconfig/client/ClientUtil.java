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

import io.vertx.core.json.Json;
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

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.apache.servicecomb.zeroconfig.ZeroConfigRegistryConstants.*;

public class ClientUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientUtil.class);

  public static final ClientUtil INSTANCE = new ClientUtil();

  private ServerMicroserviceInstance serviceInstanceForHeartbeat;

  private MulticastSocket multicastSocket;
  private InetAddress group;

  public ServerMicroserviceInstance getServiceInstanceForHeartbeat() {
    return serviceInstanceForHeartbeat;
  }

  public void setServiceInstanceForHeartbeat(
      ServerMicroserviceInstance serviceInstanceForHeartbeat) {
      this.serviceInstanceForHeartbeat = serviceInstanceForHeartbeat;
  }

  private ClientUtil(){}

  public synchronized void init() {
    try {
      group =  InetAddress.getByName(GROUP);
      multicastSocket = new MulticastSocket();
      multicastSocket.setLoopbackMode(false);
      multicastSocket.setTimeToLive(TIME_TO_LIVE);
    } catch (IOException e) {
      LOGGER.error("Failed to create MulticastSocket object", e);
    }

    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::runHeartbeatTask, CLIENT_DELAY, HEALTH_CHECK_INTERVAL,
        TimeUnit.SECONDS);
  }

  private void runHeartbeatTask(){
    if (serviceInstanceForHeartbeat != null) {
      // after first registration succeeds
      try {
        byte[] heartbeatEventDataBytes = Json.encode(serviceInstanceForHeartbeat).getBytes();
        DatagramPacket instanceDataPacket = new DatagramPacket(heartbeatEventDataBytes,
            heartbeatEventDataBytes.length, group, PORT);

        multicastSocket.send(instanceDataPacket);
      } catch (Exception e) {
        LOGGER.error("Failed to send heartbeat event for object: {}",
            serviceInstanceForHeartbeat.toString(), e);
      }
    }
  }

  public static ServerMicroserviceInstance convertToRegisterDataModel(
      MicroserviceInstance microserviceInstance, Microservice microservice) {
    ServerMicroserviceInstance instance = new ServerMicroserviceInstance();
    instance.setEvent(REGISTER_EVENT);
    instance.setVersion(microservice.getVersion());
    instance.setServiceId(microservice.getServiceId());
    instance.setInstanceId(microserviceInstance.getInstanceId());
    instance.setStatus(microserviceInstance.getStatus().toString());
    instance.setAppId(microservice.getAppId());
    instance.setServiceName(microservice.getServiceName());
    instance.setHostName(microserviceInstance.getHostName());
    instance.setEndpoints(microserviceInstance.getEndpoints());
    instance.setSchemas(microservice.getSchemas());
    return instance;
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
