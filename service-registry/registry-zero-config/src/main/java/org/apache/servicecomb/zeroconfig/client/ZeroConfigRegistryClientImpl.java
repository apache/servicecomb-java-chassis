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

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import javax.ws.rs.core.Response.Status;
import org.apache.servicecomb.foundation.vertx.AsyncResultCallback;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.serviceregistry.api.registry.ServiceCenterConfig;
import org.apache.servicecomb.serviceregistry.api.registry.ServiceCenterInfo;
import org.apache.servicecomb.registry.api.registry.FindInstancesResponse;
import org.apache.servicecomb.serviceregistry.api.response.GetSchemaResponse;
import org.apache.servicecomb.serviceregistry.api.response.HeartbeatResponse;
import org.apache.servicecomb.registry.api.event.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.zeroconfig.server.ServerMicroserviceInstance;
import org.apache.servicecomb.zeroconfig.server.ServerUtil;
import org.apache.servicecomb.zeroconfig.server.ZeroConfigRegistryService;
import org.apache.servicecomb.serviceregistry.client.http.Holder;
import org.apache.servicecomb.registry.version.Version;
import org.apache.servicecomb.registry.version.VersionRule;
import org.apache.servicecomb.registry.version.VersionRuleUtils;
import org.apache.servicecomb.registry.version.VersionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

import static org.apache.servicecomb.zeroconfig.ZeroConfigRegistryConstants.*;

public class ZeroConfigRegistryClientImpl implements ServiceRegistryClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZeroConfigRegistryClientImpl.class);

  private ZeroConfigRegistryService zeroConfigRegistryService;
  private RestTemplate restTemplate;
  private MulticastSocket multicastSocket;

  public ZeroConfigRegistryClientImpl(ZeroConfigRegistryService zeroConfigRegistryService,
      MulticastSocket multicastSocket, RestTemplate restTemplate) {
    this.zeroConfigRegistryService = zeroConfigRegistryService;
    this.restTemplate = restTemplate;
    this.multicastSocket = multicastSocket;
    try {
      this.multicastSocket.setLoopbackMode(false);
      this.multicastSocket.setTimeToLive(TIME_TO_LIVE);
    } catch (IOException e) {
      LOGGER.error("Failed configure MulticastSocket object", e);
    }
  }

  @Override
  public void init() {
  }

  @Override
  public List<Microservice> getAllMicroservices() {
    List<Microservice> allServicesList = new ArrayList<>();
    Map<String, Map<String, ServerMicroserviceInstance>> allServicesMap = ServerUtil.microserviceInstanceMap;
    allServicesMap.forEach((serviceId, instanceIdMap) -> {
      instanceIdMap.forEach((instanceId, instance) -> {
        allServicesList.add(ClientUtil.convertToClientMicroservice(instance));
      });
    });
    return allServicesList;
  }

  // this method is called before Microservice registration to check whether service with this ID exists or not
  @Override
  public String getMicroserviceId(String appId, String microserviceName, String versionRule,
      String environment) {
    return ClientUtil.getMicroserviceSelf().getServiceId();
  }

  @Override
  public String registerMicroservice(Microservice microservice) {
    String serviceId = microservice.getServiceId();
    if (serviceId == null || serviceId.length() == 0) {
      serviceId = ClientUtil.generateServiceId(microservice);
      microservice.setServiceId(serviceId);
    }
    // set to local variable so that it can be used to retrieve serviceName/appId/version when registering instance
    ClientUtil.setMicroserviceSelf(microservice);
    return serviceId;
  }

  @Override
  public Microservice getMicroservice(String microserviceId) {
    // for registration
    if (ClientUtil.getMicroserviceSelf().getServiceId().equals(microserviceId)) {
      return ClientUtil.getMicroserviceSelf();
    } else {
      // called when consumer discover provider for the very first time
      ServerMicroserviceInstance serverInstance = zeroConfigRegistryService
          .getMicroservice(microserviceId);
      return serverInstance != null ? ClientUtil.convertToClientMicroservice(serverInstance) : null;
    }
  }

  @Override
  public Microservice getAggregatedMicroservice(String microserviceId) {
    return this.getMicroservice(microserviceId);
  }

  // only used in existing UT code. Only support updating microserviceitself properties.
  @Override
  public boolean updateMicroserviceProperties(String microserviceId,
      Map<String, String> serviceProperties) {
    if (microserviceId != ClientUtil.getMicroserviceSelf().getServiceId()) {
      return false;
    }
    // putAll will update values for keys exist in the map, also add new <key, value> to the map
    ClientUtil.getMicroserviceSelf().getProperties().putAll(serviceProperties);
    return true;
  }

  @Override
  public boolean isSchemaExist(String microserviceId, String schemaId) {
    LOGGER.info("isSchemaExist: microserviceId: {}, scehmaId: {}", microserviceId, schemaId);
    String serviceId = ClientUtil.getMicroserviceSelf().getServiceId();
    if (serviceId == null || (serviceId != null && !serviceId.equals(microserviceId))) {
      throw new IllegalArgumentException("Invalid serviceId, serviceId=" + microserviceId);
    }

    List<String> schemaList = ClientUtil.getMicroserviceSelf().getSchemas();
    return schemaList != null && schemaList.contains(schemaId);
  }

  @Override
  public boolean registerSchema(String microserviceId, String schemaId, String schemaContent) {
    LOGGER.info("registerSchema: serviceId: {}, scehmaId: {}, SchemaContent: {}", microserviceId,
        schemaId, schemaContent);

    String serviceId = ClientUtil.getMicroserviceSelf().getServiceId();
    if (serviceId == null || (serviceId != null && !serviceId.equals(microserviceId))) {
      throw new IllegalArgumentException("Invalid serviceId, serviceId=" + microserviceId);
    }

    ClientUtil.getMicroserviceSelf().addSchema(schemaId, schemaContent);
    return true;
  }

  @Override
  public String getSchema(String microserviceId, String schemaId) {
    LOGGER.info("getSchema: microserviceId: {}, scehmaId: {}", microserviceId, schemaId);
    // called by service registration task when registering itself
    if (ClientUtil.getMicroserviceSelf().getServiceId().equals(microserviceId)) {
      return ClientUtil.getMicroserviceSelf().getSchemaMap().computeIfPresent(schemaId, (k, v) -> {
        return v;
      });
    } else {
      // called by consumer to load provider's schema content for the very first time
      String endpoint = this.getEndpointForMicroservice(microserviceId);
      if (endpoint == null) {
        throw new IllegalArgumentException("Provider's endpoint can NOT be Null");
      }
      String schemaContentEndpoint = endpoint + "/schemaEndpoint/schemas?schemaId=" + schemaId;
      // LOGGER.info("Going to retrieve schema content from endpoint:{}", schemaContentEndpoint);
      // Make a rest call to provider's endpoint directly to retrieve the schema content
      String schemaContent = this.restTemplate.getForObject(schemaContentEndpoint, String.class);
      //LOGGER.debug("Retrieved the schema content for microserviceId: {}, schemaId: {}, schemaContent: {}", microserviceId, schemaId, schemaContent);
      return schemaContent;
    }
  }

  private String getEndpointForMicroservice(String microserviceId) {
    ServerMicroserviceInstance serverMicroserviceInstance = zeroConfigRegistryService
        .getMicroservice(microserviceId);
    LOGGER.info("getEndpointForMicroservice: serverMicroserviceInstance: {}",
        serverMicroserviceInstance);
    if (serverMicroserviceInstance != null && !serverMicroserviceInstance.getEndpoints()
        .isEmpty()) {
      return serverMicroserviceInstance.getEndpoints().get(0)
          .replace(ENDPOINT_PREFIX_REST, ENDPOINT_PREFIX_HTTP);
    }
    return null;
  }

  @Override
  public String getAggregatedSchema(String microserviceId, String schemaId) {
    LOGGER.info("getAggregatedSchema: microserviceId: {}, scehmaId: {}", microserviceId, schemaId);
    return this.getSchema(microserviceId, schemaId);
  }

  @Override
  public Holder<List<GetSchemaResponse>> getSchemas(String microserviceId) {
    // this method is just called in MicroserviceRegisterTask.java doRegister() for registration purpose
    Holder<List<GetSchemaResponse>> resultHolder = new Holder<>();
    if (ClientUtil.getMicroserviceSelf().getServiceId() != microserviceId) {
      LOGGER.error("Invalid serviceId! Failed to retrieve microservice for serviceId {}",
          microserviceId);
      return resultHolder;
    }
    List<GetSchemaResponse> schemas = new ArrayList<>();
    ClientUtil.getMicroserviceSelf().getSchemaMap().forEach((schemaId, schemaContent) -> {
      GetSchemaResponse schema = new GetSchemaResponse();
      schema.setSchemaId(schemaId);
      schema.setSchema(schemaContent);
      schema.setSummary(
          Hashing.sha256().newHasher().putString(schemaContent, Charsets.UTF_8).hash().toString());
      schemas.add(schema);
    });
    resultHolder.setStatusCode(Status.OK.getStatusCode()).setValue(schemas);
    return resultHolder;
  }

  @Override
  public String registerMicroserviceInstance(MicroserviceInstance instance) {
    String serviceId = instance.getServiceId();
    String instanceId = instance.getInstanceId(); // allow client to set the instanceId
    if (instanceId == null || instanceId.length() == 0) {
      instanceId = ClientUtil.generateServiceInstanceId();
      instance.setInstanceId(instanceId);
    }

    try {
      Map<String, String> serviceInstanceMap = ClientUtil
          .convertToRegisterDataModel(serviceId, instanceId, instance,
              ClientUtil.getMicroserviceSelf()).get();
      byte[] instanceData = serviceInstanceMap.toString().getBytes();
      DatagramPacket instanceDataPacket = new DatagramPacket(instanceData, instanceData.length,
          InetAddress.getByName(GROUP), PORT);
      this.multicastSocket.send(instanceDataPacket);

      // set this variable for heartbeat itself status
      serviceInstanceMap.put(EVENT, HEARTBEAT_EVENT);
      ClientUtil.setServiceInstanceMapForHeartbeat(serviceInstanceMap);

    } catch (IOException e) {
      LOGGER.error("Failed to register microservice instance to mdns. servcieId: {} instanceId:{}",
          serviceId, instanceId, e);
      return null;
    }
    return instanceId;
  }

  // only used in existing UT code. get microservice instances for providerId,
  @Override
  public List<MicroserviceInstance> getMicroserviceInstance(String consumerId, String providerId) {
    List<MicroserviceInstance> microserviceInstanceResultList = new ArrayList<>();
    Optional<List<ServerMicroserviceInstance>> optionalServerMicroserviceInstanceList = this.zeroConfigRegistryService
        .
            getMicroserviceInstance(consumerId, providerId);
    if (optionalServerMicroserviceInstanceList.isPresent()) {
      microserviceInstanceResultList = optionalServerMicroserviceInstanceList.get().stream()
          .map(serverInstance -> {
            return ClientUtil.convertToClientMicroserviceInstance(serverInstance);
          }).collect(Collectors.toList());
    } else {
      LOGGER.error("Invalid serviceId: {}", providerId);
    }
    return microserviceInstanceResultList;
  }

  // only used in existing UT code. Only support updating microserviceitself instance properties.
  @Override
  public boolean updateInstanceProperties(String microserviceId, String instanceId,
      Map<String, String> instanceProperties) {
    Microservice selfMicroservice = ClientUtil.getMicroserviceSelf();
    MicroserviceInstance selfInstance = selfMicroservice.getInstance();
    if (selfInstance == null || !(selfInstance.getInstanceId().equals(instanceId))
        || !(selfMicroservice.getServiceId().equals(microserviceId))) {
      LOGGER.error(
          "Invalid microserviceId, microserviceId: {} OR microserviceInstanceId, microserviceInstanceId: {}",
          microserviceId, instanceId);
      return false;
    }

    // putAll will update values for keys exist in the map, also add new <key, value> to the map
    selfInstance.getProperties().putAll(instanceProperties);
    ClientUtil.getMicroserviceSelf().setInstance(selfInstance);
    return true;
  }

  @Override
  public boolean unregisterMicroserviceInstance(String serviceId, String instanceId) {
    Optional<ServerMicroserviceInstance> optionalServerMicroserviceInstance = this.zeroConfigRegistryService
        .findServiceInstance(serviceId, instanceId);

    if (optionalServerMicroserviceInstance.isPresent()) {
      try {
        LOGGER.info(
            "Start unregister microservice instance. The instance with servcieId: {} instanceId:{}",
            serviceId, instanceId);
        Map<String, String> unregisterEventMap = new HashMap<>();
        unregisterEventMap.put(EVENT, UNREGISTER_EVENT);
        unregisterEventMap.put(SERVICE_ID, serviceId);
        unregisterEventMap.put(INSTANCE_ID, instanceId);
        byte[] unregisterEventBytes = unregisterEventMap.toString().getBytes();
        DatagramPacket unregisterEventDataPacket = new DatagramPacket(unregisterEventBytes,
            unregisterEventBytes.length,
            InetAddress.getByName(GROUP), PORT);
        this.multicastSocket.send(unregisterEventDataPacket);
        return true;
      } catch (IOException e) {
        LOGGER
            .error("Failed to unregister microservice instance event. servcieId: {} instanceId:{}",
                serviceId, instanceId, e);
        return false;
      }
    }
    return false;
  }

  @Override
  public HeartbeatResponse heartbeat(String microserviceId, String instanceId) {
    HeartbeatResponse response = new HeartbeatResponse();
    if (this.zeroConfigRegistryService.heartbeat(microserviceId, instanceId)) {
      response.setMessage(INSTANCE_HEARTBEAT_RESPONSE_MESSAGE_OK);
      response.setOk(true);
    }
    return response;
  }

  @Override
  public void watch(String selfMicroserviceId,
      AsyncResultCallback<MicroserviceInstanceChangedEvent> callback) {
  }

  @Override
  public void watch(String selfMicroserviceId,
      AsyncResultCallback<MicroserviceInstanceChangedEvent> callback,
      AsyncResultCallback<Void> onOpen, AsyncResultCallback<Void> onClose) {
  }


  @Override
  public List<MicroserviceInstance> findServiceInstance(String selfMicroserviceId, String appId,
      String serviceName, String versionRule) {
    MicroserviceInstances instances = findServiceInstances(selfMicroserviceId, appId, serviceName,
        versionRule, null);
    if (instances.isMicroserviceNotExist()) {
      return null;
    }
    return instances.getInstancesResponse().getInstances();
  }

  /**
   * called by RefreshableMicroserviceCache.pullInstanceFromServiceCenter when consumer call
   * provider for the first time https://github.com/apache/servicecomb-service-center/blob/master/server/core/swagger/v4.yaml
   *
   * @param consumerId           in http header, NOT the query parameters ("X-ConsumerId",
   *                             consumerId);
   * @param appId:               = appId (Server side, Required)
   * @param providerServiceName: = serviceName (Server side, Required)
   * @param strVersionRule:      = version (Server side, Required) (e.g. 0.0.0.0+") 1.精确版本匹配
   *                             2.后续版本匹配 3.最新版本 4.版本范围
   * @param revision:            = rev (Server side, Optional. is null) for compatible with existing
   *                             system
   * @return MicroserviceInstances  collection of Microservice Instance for (appId,
   * providerServiceName, strVersionRule)
   */
  @Override
  public MicroserviceInstances findServiceInstances(String consumerId, String appId,
      String providerServiceName, String strVersionRule, String revision) {
    LOGGER.info(
        "find service instance for consumerId: {}, providerServiceName: {}, versionRule: {}, revision: {}",
        consumerId, providerServiceName, strVersionRule, revision);

    MicroserviceInstances resultMicroserviceInstances = new MicroserviceInstances();
    FindInstancesResponse response = new FindInstancesResponse();
    List<MicroserviceInstance> resultInstanceList = new ArrayList<>();

    // 1.  find matched appId and serviceName from "Server"
    List<ServerMicroserviceInstance> tempServerInstanceList = this.zeroConfigRegistryService.
        findServiceInstances(appId, providerServiceName);

    // 2.  find matched instance based on the strVersionRule
    VersionRule versionRule = VersionRuleUtils.getOrCreate(strVersionRule);

    ServerMicroserviceInstance latestVersionInstance = findLatestVersionInstance(
        tempServerInstanceList, versionRule);
    if (latestVersionInstance != null) {
      Version latestVersion = VersionUtils.getOrCreate(latestVersionInstance.getVersion());
      for (ServerMicroserviceInstance serverInstance : tempServerInstanceList) {
        Version version = VersionUtils.getOrCreate(serverInstance.getVersion());
        if (!versionRule.isMatch(version, latestVersion)) {
          continue;
        }
        resultInstanceList.add(ClientUtil.convertToClientMicroserviceInstance(serverInstance));
      }
    }

    response.setInstances(resultInstanceList);
    resultMicroserviceInstances.setInstancesResponse(response);
    return resultMicroserviceInstances;
  }

  private ServerMicroserviceInstance findLatestVersionInstance(
      List<ServerMicroserviceInstance> instanceList, VersionRule versionRule) {
    Version latestVersion = null;
    ServerMicroserviceInstance latestVersionInstance = null;
    for (ServerMicroserviceInstance serverInstance : instanceList) {
      Version version = VersionUtils.getOrCreate(serverInstance.getVersion());
      if (!versionRule.isAccept(version)) {
        continue;
      }

      if (latestVersion == null || version.compareTo(latestVersion) > 0) {
        latestVersion = version;
        latestVersionInstance = serverInstance;
      }
    }
    return latestVersionInstance;
  }


  @Override
  public MicroserviceInstance findServiceInstance(String serviceId, String instanceId) {
    Optional<ServerMicroserviceInstance> optionalServerMicroserviceInstance = this.zeroConfigRegistryService
        .
            findServiceInstance(serviceId, instanceId);

    if (optionalServerMicroserviceInstance.isPresent()) {
      return ClientUtil
          .convertToClientMicroserviceInstance(optionalServerMicroserviceInstance.get());
    } else {
      LOGGER.error(
          "Invalid serviceId OR instanceId! Failed to retrieve Microservice Instance for serviceId {} and instanceId {}",
          serviceId, instanceId);
      return null;
    }
  }

  // for compatibility with existing registration logic. only used in the existing UT code.
  @Override
  public ServiceCenterInfo getServiceCenterInfo() {
    ServiceCenterInfo info = new ServiceCenterInfo();
    info.setVersion("");
    info.setBuildTag("");
    info.setRunMode("");
    info.setApiVersion("");
    info.setConfig(new ServiceCenterConfig());
    return info;
  }

  /**
   * for compatibility with existing registration logic and flow. 1. Only called by
   * SCBEngine.turnDownInstanceStatus to set instance status to Down 2. In zero-config context,
   * there is no need to update status (from UP TO DOWN)as there is no physical registry center to
   * show the status
   */
  @Override
  public boolean updateMicroserviceInstanceStatus(String microserviceId, String instanceId,
      MicroserviceInstanceStatus status) {
    if (null == status) {
      throw new IllegalArgumentException("null status is now allowed");
    }
    String selfServiceId = ClientUtil.getMicroserviceSelf().getServiceId();
    MicroserviceInstance selfInstance = ClientUtil.getMicroserviceSelf().getInstance();

    if (!microserviceId.equals(selfServiceId) || selfInstance == null || !selfInstance
        .getInstanceId().equals(instanceId)) {
      throw new IllegalArgumentException(
          String.format("Invalid argument. microserviceId=%s, instanceId=%s.",
              microserviceId,
              instanceId));
    }
    selfInstance.setStatus(status);
    return this.unregisterMicroserviceInstance(microserviceId, instanceId);
  }

}
