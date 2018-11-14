package org.apache.servicecomb.springboot2.starter.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryContext;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryFilter;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;

class InstanceDiscoveryFilter implements DiscoveryFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstanceDiscoveryFilter.class);

  @Override
  public int getOrder() {
    return Short.MAX_VALUE;
  }

  @Override
  public DiscoveryTreeNode discovery(DiscoveryContext context, DiscoveryTreeNode parent) {
    return parent.children()
        .computeIfAbsent(context.getInputParameters(), etn -> createDiscoveryTreeNode(context, parent));
  }

  @SuppressWarnings("unchecked")
  protected DiscoveryTreeNode createDiscoveryTreeNode(DiscoveryContext context,
      DiscoveryTreeNode parent) {
    String serviceName = context.getInputParameters();
    List<ServiceInstance> instances = new ArrayList<>();
    for (MicroserviceInstance instance : ((Map<String, MicroserviceInstance>) parent.data()).values()) {
      for (String endpoint : instance.getEndpoints()) {
        String scheme = endpoint.split(":", 2)[0];
        if (!scheme.equalsIgnoreCase(Const.RESTFUL)) {
          LOGGER.info("Endpoint {} is not supported in Spring Cloud, ignoring.", endpoint);
          continue;
        }
        URIEndpointObject uri = new URIEndpointObject(endpoint);
        instances.add(new DefaultServiceInstance(serviceName, uri.getHostOrIp(), uri.getPort(), uri.isSslEnabled()));
      }
    }

    return new DiscoveryTreeNode()
        .subName(parent, serviceName)
        .data(instances);
  }
};