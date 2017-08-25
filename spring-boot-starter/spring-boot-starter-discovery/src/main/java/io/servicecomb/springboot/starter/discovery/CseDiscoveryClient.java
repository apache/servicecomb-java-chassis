/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.springboot.starter.discovery;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import io.servicecomb.core.provider.consumer.ConsumerProviderManager;
import io.servicecomb.core.provider.consumer.ReferenceConfig;
import io.servicecomb.foundation.common.net.URIEndpointObject;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.client.ServiceRegistryClient;

public class CseDiscoveryClient implements DiscoveryClient {

  @Inject
  private ConsumerProviderManager consumerProviderManager;

  @Override
  public String description() {
    return "Spring Cloud CSE Discovery Client";
  }

  @Override
  public List<ServiceInstance> getInstances(final String serviceId) {
    List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
    ServiceRegistryClient client = RegistryUtils.getServiceRegistryClient();
    String appId = RegistryUtils.getAppId();
    ReferenceConfig referenceConfig = consumerProviderManager.getReferenceConfig(serviceId);
    String versionRule = referenceConfig.getMicroserviceVersionRule();
    String cseServiceID = client.getMicroserviceId(appId, serviceId, versionRule);
    List<MicroserviceInstance> cseServices = client.getMicroserviceInstance(cseServiceID, cseServiceID);
    if (null != cseServices && !cseServices.isEmpty()) {
      for (MicroserviceInstance instance : cseServices) {
        List<String> eps = instance.getEndpoints();
        for (String ep : eps) {
          URIEndpointObject uri = new URIEndpointObject(ep);
          instances.add(new DefaultServiceInstance(instance.getServiceId(), uri.getHostOrIp(),
              uri.getPort(), false));
        }
      }
    }
    return instances;
  }

  @Override
  public ServiceInstance getLocalServiceInstance() {
    return null;
  }

  @Override
  public List<String> getServices() {
    ServiceRegistryClient client = RegistryUtils.getServiceRegistryClient();
    List<Microservice> services = client.getAllMicroservices();
    List<String> serviceIDList = new ArrayList<>();
    if (null != services && !services.isEmpty()) {
      for (Microservice service : services) {
        serviceIDList.add(service.getServiceName());
      }
    }
    return serviceIDList;
  }
}
