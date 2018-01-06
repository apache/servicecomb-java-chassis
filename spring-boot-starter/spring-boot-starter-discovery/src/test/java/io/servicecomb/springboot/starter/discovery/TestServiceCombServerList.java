package io.servicecomb.springboot.starter.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.Server;

import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.discovery.DiscoveryContext;
import io.servicecomb.serviceregistry.discovery.DiscoveryTree;
import io.servicecomb.serviceregistry.discovery.DiscoveryTreeNode;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;

public class TestServiceCombServerList {
  @Test
  public void testServiceCombServerList(@Injectable IClientConfig iClientConfig,
      @Mocked RegistryUtils registryUtils,
      @Mocked DiscoveryTree discoveryTree,
      @Injectable DiscoveryTreeNode versionedCache) {
    Map<String, MicroserviceInstance> servers = new HashMap<>();
    List<String> endpoints = new ArrayList<>();
    endpoints.add("rest://localhost:3333");
    endpoints.add("rest://localhost:4444");
    MicroserviceInstance instance1 = new MicroserviceInstance();
    instance1.setServiceId("service1");
    instance1.setInstanceId("service1-instance1");
    instance1.setEndpoints(endpoints);
    servers.put("service1-instance1", instance1);

    new Expectations() {
      {
        iClientConfig.getClientName();
        result = "serviceId1";

        RegistryUtils.getAppId();
        result = "app";
        discoveryTree.discovery((DiscoveryContext) any, anyString, anyString, anyString);
        result = versionedCache;
        versionedCache.data();
        result = servers;
      }
    };

    ServiceCombServerList list = new ServiceCombServerList();
    list.initWithNiwsConfig(iClientConfig);
    List<Server> serverList = list.getInitialListOfServers();
    Assert.assertEquals(2, serverList.size());
    Assert.assertEquals(4444, serverList.get(1).getPort());
  }
}
