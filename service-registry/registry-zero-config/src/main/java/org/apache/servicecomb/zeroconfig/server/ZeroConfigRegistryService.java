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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.servicecomb.zeroconfig.ZeroConfigRegistryConstants.*;

public class ZeroConfigRegistryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZeroConfigRegistryService.class);

  public void registerMicroserviceInstance(Map<String, String> serviceAttributeMap) {
    String instanceId = serviceAttributeMap.get(INSTANCE_ID);
    String serviceId = serviceAttributeMap.get(SERVICE_ID);
    String serviceName = serviceAttributeMap.get(SERVICE_NAME);

    if (serviceId == null || serviceName == null || instanceId == null) {
      throw new IllegalArgumentException("Invalid serviceId, serviceId=" + serviceId +
          "Invalid serviceName, serviceName=" + serviceName + "Invalid instanceId, instanceId="
          + instanceId);
    }

    // convert to server side ServerMicroserviceInstance object
    ServerMicroserviceInstance newServerMicroserviceInstance = ServerUtil
        .convertToServerMicroserviceInstance(serviceAttributeMap);
    Map<String, ServerMicroserviceInstance> innerInstanceMap = ServerUtil.microserviceInstanceMap.
        computeIfAbsent(serviceId, id -> new ConcurrentHashMap<>());

    if (innerInstanceMap.containsKey(instanceId)) {
      LOGGER.info("ServiceId: {}, instanceId: {} already exists", serviceId, instanceId);
    } else {
      // register a new instance for the service
      LOGGER
          .info("Register a new instance for  serviceId: {}, instanceId: {}, status: {}, name: {}",
              serviceId,
              instanceId, newServerMicroserviceInstance.getStatus(),
              newServerMicroserviceInstance.getServiceName());
      innerInstanceMap.put(instanceId, newServerMicroserviceInstance);
    }
  }

  public void unregisterMicroserviceInstance(Map<String, String> serviceAttributeMap) {
    String unregisterServiceId = serviceAttributeMap.get(SERVICE_ID);
    String unregisterInstanceId = serviceAttributeMap.get(INSTANCE_ID);

    if (unregisterServiceId == null || unregisterInstanceId == null) {
      throw new IllegalArgumentException(
          "Invalid unregisterServiceId, unregisterServiceId=" + unregisterServiceId +
              "Invalid unregisterInstanceId, unregisterInstanceId=" + unregisterInstanceId);
    }

    ServerUtil.microserviceInstanceMap
        .computeIfPresent(unregisterServiceId, (serviceId, instanceIdMap) -> {
          instanceIdMap.computeIfPresent(unregisterInstanceId, (instanceId, instance) -> {
            // remove this instance from inner instance map
            LOGGER.info(
                "Successfully unregistered/remove serviceId: {}, instanceId: {} from server side",
                unregisterServiceId, unregisterInstanceId);
            return null;
          });
          // if the inner instance map is empty, remove the service itself from the outer map too
          return !instanceIdMap.isEmpty() ? instanceIdMap : null;
        });

  }

  public ServerMicroserviceInstance findServiceInstance(String serviceId,
      String instanceId) {
    Map<String, ServerMicroserviceInstance> serverMicroserviceInstanceMap = ServerUtil.microserviceInstanceMap
        .get(serviceId);
    if (serverMicroserviceInstanceMap == null || serverMicroserviceInstanceMap.isEmpty()) {
      return null;
    }
    return serverMicroserviceInstanceMap.get(instanceId);
  }

  public List<ServerMicroserviceInstance> getMicroserviceInstance(String consumerId,
      String providerId) {
    Map<String, ServerMicroserviceInstance> instanceIdMap = ServerUtil.microserviceInstanceMap
        .get(providerId);
    if (instanceIdMap == null || instanceIdMap.isEmpty()) {
      throw new IllegalArgumentException("Invalid serviceId, serviceId=" + providerId);
    }
    return new ArrayList<>(instanceIdMap.values());
  }

  // for scenario: when other service started before this one start
  public void heartbeat(Map<String, String> heartbeatEventMap) {
    String serviceId = heartbeatEventMap.get(SERVICE_ID);
    String instanceId = heartbeatEventMap.get(INSTANCE_ID);

    Map<String, ServerMicroserviceInstance> serverMicroserviceInstanceMap = ServerUtil.microserviceInstanceMap
        .get(serviceId);
    if (serverMicroserviceInstanceMap != null && serverMicroserviceInstanceMap
        .containsKey(instanceId)) {
      ServerMicroserviceInstance instance = serverMicroserviceInstanceMap.get(instanceId);
      instance.setLastHeartbeatTimeStamp(Instant.now());
    } else {
      heartbeatEventMap.put(EVENT, REGISTER_EVENT);
      LOGGER.info(
          "Received HEARTBEAT event from serviceId: {}, instancdId: {} for the first time. Register it instead.",
          serviceId, instanceId);
      this.registerMicroserviceInstance(heartbeatEventMap);
    }
  }

  // for compatibility with existing registration workflow
  public boolean heartbeat(String microserviceId, String microserviceInstanceId) {
    Map<String, ServerMicroserviceInstance> serverMicroserviceInstanceMap = ServerUtil.microserviceInstanceMap
        .get(microserviceId);
    return serverMicroserviceInstanceMap != null && serverMicroserviceInstanceMap
        .containsKey(microserviceInstanceId);
  }

  // for compatibility with existing registration workflow
  public ServerMicroserviceInstance getMicroservice(String microserviceId) {
    Map<String, ServerMicroserviceInstance> instanceIdMap = ServerUtil.microserviceInstanceMap
        .get(microserviceId);
    if (instanceIdMap != null) {
      List<ServerMicroserviceInstance> serverMicroserviceInstanceList = new ArrayList<>(
          instanceIdMap.values());
      return serverMicroserviceInstanceList.get(0);
    }
    return null;
  }

  public List<ServerMicroserviceInstance> findServiceInstances(String appId, String serviceName) {
    List<ServerMicroserviceInstance> resultInstanceList = new ArrayList<>();
    ServerUtil.microserviceInstanceMap.forEach((serviceId, instanceIdMap) -> {
      instanceIdMap.forEach((instanceId, instance) -> {
        // match appId and ServiceName
        if (appId.equals(instance.getAppId()) && serviceName.equals(instance.getServiceName())) {
          resultInstanceList.add(instance);
        }
      });
    });
    return resultInstanceList;
  }
}
