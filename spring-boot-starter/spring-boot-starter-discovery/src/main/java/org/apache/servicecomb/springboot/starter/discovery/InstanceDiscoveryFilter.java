package org.apache.servicecomb.springboot.starter.discovery;

import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.springboot.common.AbstractInstanceDiscoveryFilter;
import org.springframework.cloud.client.DefaultServiceInstance;

class InstanceDiscoveryFilter extends AbstractInstanceDiscoveryFilter {
  @Override
  protected Object createInstance(String name, URIEndpointObject uri) {
    return new DefaultServiceInstance(name, uri.getHostOrIp(), uri.getPort(), uri.isSslEnabled());
  }
}