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

package org.apache.servicecomb.localservice;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestLocalServicePriorityFilter {
  private static final String SELF_HOST;

  private static final boolean HAS_REAL_LOCAL_IP;

  static {
    String host;
    boolean real;
    try {
      InetAddress localHost = InetAddress.getLocalHost();
      host = localHost.getHostAddress();
      real = !localHost.isLoopbackAddress();
    } catch (Exception e) {
      host = "127.0.0.1";
      real = false;
    }
    SELF_HOST = host;
    HAS_REAL_LOCAL_IP = real;
  }

  private final LocalServicePriorityFilter filter = new LocalServicePriorityFilter();

  private static void assumeRealLocalIp() {
    Assumptions.assumeTrue(HAS_REAL_LOCAL_IP,
        "test machine only has loopback address, local-priority tests are skipped");
  }

  @BeforeEach
  public void setUp() {
    LocalServicePriorityFilter.clearWeightCache();
    LocalAddressCache.reset();
  }

  @AfterEach
  public void tearDown() {
    LocalServicePriorityFilter.clearWeightCache();
    LocalAddressCache.reset();
  }

  static DiscoveryInstance instance(String instanceId, String hostOrIp) {
    return instance(instanceId, hostOrIp, null);
  }

  static DiscoveryInstance instance(String instanceId, String hostOrIp, Map<String, String> properties) {
    return new StubInstance(instanceId, hostOrIp, properties);
  }

  /**
   * A provider instance that supports local service, i.e. a P1 candidate.
   */
  static DiscoveryInstance localProvider(String instanceId, String hostOrIp) {
    Map<String, String> properties = new HashMap<>();
    properties.put("supportLocalService", "true");
    return instance(instanceId, hostOrIp, properties);
  }

  static ServiceCombServer server(DiscoveryInstance instance) {
    return server("rest://" + ((StubInstance) instance).hostOrIp + ":8080", instance);
  }

  static ServiceCombServer server(String endpoint, DiscoveryInstance instance) {
    return new ServiceCombServer("provider", new Endpoint(dummyTransport(), endpoint, instance));
  }

  /**
   * A Transport stub only good for building Endpoint objects; the endpoint string
   * is never dialed in unit tests. (Avoids Mockito inline-mock-maker attachment
   * flakiness on some JVMs.)
   */
  static Transport dummyTransport() {
    return new Transport() {
      @Override
      public String getName() {
        return "dummy";
      }

      @Override
      public boolean init() {
        return false;
      }

      @Override
      public void setEnvironment(org.springframework.core.env.Environment environment) {
        // no-op
      }

      @Override
      public Object parseAddress(String endpoint) {
        return null;
      }

      @Override
      public Endpoint getEndpoint() {
        return null;
      }

      @Override
      public Endpoint getPublishEndpoint() {
        return null;
      }
    };
  }

  private static void assertLocalEndpoint(ServiceCombServer server) {
    Assertions.assertEquals("rest://127.0.0.1:8080", server.getEndpoint().getEndpoint());
  }

  // ------------------------------------------------------------------ basics

  @Test
  public void getFilteredListOfServersKeepsNullAndEmptyList() {
    Assertions.assertNull(filter.getFilteredListOfServers(null, null));
    Assertions.assertTrue(filter.getFilteredListOfServers(new ArrayList<>(), null).isEmpty());
  }

  @Test
  public void orderIs250AfterZoneAware() {
    Assertions.assertEquals(250, filter.getOrder());
  }

  // ------------------------------------------------------------- host identify

  @Test
  public void hostIdentifierPrefersProperty() {
    Map<String, String> properties = new HashMap<>();
    properties.put(LocalServicePriorityFilter.PROPERTY_HOST_NAME, "host-1");
    DiscoveryInstance inst = instance("i1", "10.1.1.1", properties);
    Assertions.assertEquals("host-1", filter.getHostIdentifier(inst));
  }

  @Test
  public void hostIdentifierFallsBackToEndpoint() {
    DiscoveryInstance inst = instance("i1", "10.1.1.1");
    Assertions.assertEquals("10.1.1.1", filter.getHostIdentifier(inst));
  }

  @Test
  public void hostIdentifierEmptyForNullInstance() {
    Assertions.assertEquals("", filter.getHostIdentifier(null));
  }

  // ------------------------------------------------------ weight calculations

  @Test
  public void noTopologyInfoFallsBackToEqualDistribution() {
    LocalServicePriorityFilter.InstanceWeights weights = new LocalServicePriorityFilter.InstanceWeights();
    List<ServiceCombServer> servers = List.of(
        server(instance("p1", "10.0.0.1")),
        server(instance("p2", "10.0.0.2")));

    filter.calculateInstanceWeights(weights, servers, List.of());

    Assertions.assertNull(weights.p1);
    Assertions.assertEquals(2, weights.p3List.size());
    Assertions.assertEquals(0.5, weights.p3WeightEach, 0.0001);
  }

  @Test
  public void cEqualsPWithP1AllTrafficToP1() {
    assumeRealLocalIp();
    DiscoveryInstance self = instance("self", SELF_HOST);
    List<? extends DiscoveryInstance> consumers = List.of(
        self,
        instance("other-consumer", "10.0.0.2"));
    List<ServiceCombServer> servers = List.of(
        server("rest://" + SELF_HOST + ":8080", localProvider("p1", SELF_HOST)),
        server(instance("p2", "10.0.0.2")));

    LocalServicePriorityFilter.InstanceWeights weights = new LocalServicePriorityFilter.InstanceWeights();
    filter.calculateInstanceWeights(weights, servers, consumers);

    Assertions.assertNotNull(weights.p1);
    Assertions.assertEquals(1.0, weights.p1Weight, 0.0001);
    Assertions.assertEquals(0, weights.p2WeightEach, 0.0001);
    Assertions.assertEquals(0, weights.p3WeightEach, 0.0001);

    // selection must always be the local instance with rewritten endpoint
    for (int i = 0; i < 100; i++) {
      ServiceCombServer selected = filter.selectByWeights(weights);
      Assertions.assertSame(weights.p1.getEndpoint().getMicroserviceInstance(),
          selected.getEndpoint().getMicroserviceInstance());
      assertLocalEndpoint(selected);
    }
  }

  @Test
  public void cGreaterThanPWithoutP1DistributesToP2AndP3() {
    List<? extends DiscoveryInstance> consumers = List.of(
        instance("c-self", "10.0.0.1"),
        instance("c-2", "10.0.0.2"),
        instance("c-3", "10.0.0.3"),
        instance("c-4", "10.0.0.4"));
    // this consumer is on 10.0.0.1, which has no provider -> no P1
    List<ServiceCombServer> servers = List.of(
        server(instance("p-colocated", "10.0.0.2")),
        server(instance("p-remote", "10.0.0.9")));

    LocalServicePriorityFilter.InstanceWeights weights = new LocalServicePriorityFilter.InstanceWeights();
    filter.calculateInstanceWeights(weights, servers, consumers);

    Assertions.assertNull(weights.p1);
    Assertions.assertEquals(1, weights.p2List.size());
    Assertions.assertEquals(1, weights.p3List.size());
    // 1/P - 1/C = 0.5 - 0.25
    Assertions.assertEquals(0.25, weights.p2WeightEach, 0.0001);
    Assertions.assertEquals(0.5, weights.p3WeightEach, 0.0001);
  }

  @Test
  public void cLessThanPWithP1GetsProportionalTraffic() {
    assumeRealLocalIp();
    DiscoveryInstance self = instance("self", SELF_HOST);
    List<? extends DiscoveryInstance> consumers = List.of(
        self,
        instance("other-consumer", "10.0.0.2"));
    List<ServiceCombServer> servers = List.of(
        server("rest://" + SELF_HOST + ":8080", localProvider("p1", SELF_HOST)),
        server(instance("p2", "10.0.0.2")),
        server(instance("p3-1", "10.0.0.3")),
        server(instance("p3-2", "10.0.0.4")));

    LocalServicePriorityFilter.InstanceWeights weights = new LocalServicePriorityFilter.InstanceWeights();
    filter.calculateInstanceWeights(weights, servers, consumers);

    Assertions.assertNotNull(weights.p1);
    // C/P = 2/4
    Assertions.assertEquals(0.5, weights.p1Weight, 0.0001);
    Assertions.assertEquals(0, weights.p2WeightEach, 0.0001);
    Assertions.assertEquals(0.25, weights.p3WeightEach, 0.0001);

    // statistical check: local share ~ C/P
    int localCount = 0;
    int samples = 40000;
    for (int i = 0; i < samples; i++) {
      if (filter.selectByWeights(weights).getEndpoint().getEndpoint().startsWith("rest://127.0.0.1")) {
        localCount++;
      }
    }
    double localRatio = (double) localCount / samples;
    Assertions.assertEquals(0.5, localRatio, 0.05, "local ratio should be ~C/P");
  }

  @Test
  public void cLessThanPWithoutP1AllTrafficToP3() {
    DiscoveryInstance self = instance("self", SELF_HOST);
    List<? extends DiscoveryInstance> consumers = List.of(
        self,
        instance("other-consumer", "10.0.0.2"));
    // no provider on self host -> no P1
    List<ServiceCombServer> servers = List.of(
        server(instance("p2", "10.0.0.2")),
        server(instance("p3-1", "10.0.0.3")),
        server(instance("p3-2", "10.0.0.4")));

    LocalServicePriorityFilter.InstanceWeights weights = new LocalServicePriorityFilter.InstanceWeights();
    filter.calculateInstanceWeights(weights, servers, consumers);

    Assertions.assertNull(weights.p1);
    Assertions.assertEquals(0, weights.p2WeightEach, 0.0001);
    Assertions.assertEquals(1.0 / 3, weights.p3WeightEach, 0.0001);

    // the provider colocated with the other consumer gets zero weight
    ServiceCombServer p2 = weights.p2List.get(0);
    for (int i = 0; i < 100; i++) {
      Assertions.assertNotSame(p2, filter.selectByWeights(weights));
    }
  }

  @Test
  public void p1RequiresSupportLocalServiceProperty() {
    DiscoveryInstance self = instance("self", SELF_HOST);
    List<? extends DiscoveryInstance> consumers = List.of(self);
    Map<String, String> withoutSupport = new HashMap<>();
    withoutSupport.put(LocalServicePriorityFilter.PROPERTY_HOST_NAME, SELF_HOST);
    List<ServiceCombServer> servers = List.of(
        server("rest://" + SELF_HOST + ":8080", instance("p-local", SELF_HOST, withoutSupport)));

    LocalServicePriorityFilter.InstanceWeights weights = new LocalServicePriorityFilter.InstanceWeights();
    filter.calculateInstanceWeights(weights, servers, consumers);

    // provider on same host but not supporting local service is not P1
    Assertions.assertNull(weights.p1);
    Assertions.assertEquals(1, weights.p2List.size());
  }

  @Test
  public void selfHostNotFoundMeansNoP1() {
    // consumers on fake addresses not resolvable to this machine,
    // self instance id can not be found (no engine in unit test)
    List<? extends DiscoveryInstance> consumers = List.of(
        instance("c-1", "10.200.200.1"),
        instance("c-2", "10.200.200.2"));
    List<ServiceCombServer> servers = List.of(
        server(instance("p1", "10.200.200.1")));

    LocalServicePriorityFilter.InstanceWeights weights = new LocalServicePriorityFilter.InstanceWeights();
    filter.calculateInstanceWeights(weights, servers, consumers);

    Assertions.assertNull(weights.p1);
  }

  @Test
  public void selfHostFoundByLocalAddressFallback() {
    assumeRealLocalIp();
    List<? extends DiscoveryInstance> consumers = List.of(
        instance("c-1", SELF_HOST),
        instance("c-2", "10.200.200.2"));
    List<ServiceCombServer> servers = List.of(
        server(localProvider("p1", SELF_HOST)));

    LocalServicePriorityFilter.InstanceWeights weights = new LocalServicePriorityFilter.InstanceWeights();
    filter.calculateInstanceWeights(weights, servers, consumers);

    Assertions.assertNotNull(weights.p1);
  }

  // --------------------------------------------------------------- cache

  @Test
  public void cacheIsReusedWhileServerListUnchanged() {
    List<? extends DiscoveryInstance> consumers = List.of(instance("c-1", SELF_HOST));
    List<ServiceCombServer> servers = List.of(server(instance("p1", SELF_HOST)));

    LocalServicePriorityFilter.InstanceWeights first =
        filter.getOrCreateWeights("app:provider", servers);
    Assertions.assertSame(first, filter.getOrCreateWeights("app:provider", servers));
  }

  @Test
  public void cacheInvalidWhenProviderListChanged() {
    List<? extends DiscoveryInstance> consumers = List.of(instance("c-1", SELF_HOST));
    List<ServiceCombServer> servers = new ArrayList<>();
    servers.add(server(instance("p1", SELF_HOST)));
    LocalServicePriorityFilter.InstanceWeights cached =
        filter.getOrCreateWeights("app:provider", servers);

    Assertions.assertTrue(filter.isCacheValid(cached, servers));

    List<ServiceCombServer> changed = new ArrayList<>();
    changed.add(server(instance("p1", SELF_HOST)));
    changed.add(server(instance("p2", "10.0.0.5")));
    Assertions.assertFalse(filter.isCacheValid(cached, changed));
  }

  @Test
  public void clearWeightCacheRemovesAllEntries() {
    // simulate the change listener: self service change clears everything
    List<? extends DiscoveryInstance> consumers = List.of(instance("c-1", SELF_HOST));
    List<ServiceCombServer> servers = List.of(server(instance("p1", SELF_HOST)));
    filter.getOrCreateWeights("app:provider", servers);
    Assertions.assertFalse(LocalServicePriorityFilter.cacheView().isEmpty());

    LocalServicePriorityFilter.clearWeightCache();
    Assertions.assertTrue(LocalServicePriorityFilter.cacheView().isEmpty());
  }

  // ------------------------------------------------------ create local server

  @Test
  public void createLocalServerRewritesEndpointToLoopback() {
    assumeRealLocalIp();
    DiscoveryInstance inst = instance("p1", SELF_HOST);
    ServiceCombServer original = server("rest://" + SELF_HOST + ":8080", inst);

    ServiceCombServer local = filter.createLocalServer(original);

    Assertions.assertNotSame(original, local);
    Assertions.assertEquals("provider", local.getMicroserviceName());
    Assertions.assertSame(inst, local.getEndpoint().getMicroserviceInstance());
    assertLocalEndpoint(local);
    // original server is untouched
    Assertions.assertEquals("rest://" + SELF_HOST + ":8080", original.getEndpoint().getEndpoint());
  }

  @Test
  public void createLocalServerDropsSslAndQueryParameters() {
    assumeRealLocalIp();
    DiscoveryInstance inst = instance("p1", SELF_HOST);
    ServiceCombServer original = server("rest://" + SELF_HOST + ":8080?sslEnabled=true&protocol=2", inst);

    ServiceCombServer local = filter.createLocalServer(original);

    Assertions.assertEquals("rest://127.0.0.1:8080", local.getEndpoint().getEndpoint());
  }

  @Test
  public void createLocalServerKeepsOriginalWhenPortMissing() {
    DiscoveryInstance inst = instance("p1", SELF_HOST);
    ServiceCombServer original = server("rest://" + SELF_HOST, inst);

    Assertions.assertSame(original, filter.createLocalServer(original));
  }

  @Test
  public void createLocalServerNull() {
    Assertions.assertNull(filter.createLocalServer(null));
  }

  // ---------------------------------------------------- selectByWeights edge

  @Test
  public void selectByWeightsFallsBackToP1WhenTotalWeightZero() {
    assumeRealLocalIp();
    LocalServicePriorityFilter.InstanceWeights weights = new LocalServicePriorityFilter.InstanceWeights();
    weights.p1 = server(instance("p1", SELF_HOST));

    ServiceCombServer selected = filter.selectByWeights(weights);
    Assertions.assertNotNull(selected);
    assertLocalEndpoint(selected);
  }

  @Test
  public void selectByWeightsFallsBackToP3WhenTotalWeightZero() {
    LocalServicePriorityFilter.InstanceWeights weights = new LocalServicePriorityFilter.InstanceWeights();
    weights.p3List = new ArrayList<>(List.of(server(instance("p3", "10.0.0.9"))));

    Assertions.assertSame(weights.p3List.get(0), filter.selectByWeights(weights));
  }

  @Test
  public void selectByWeightsReturnsNullWhenNothingSelectable() {
    Assertions.assertNull(filter.selectByWeights(new LocalServicePriorityFilter.InstanceWeights()));
  }

  // ------------------------------------------------------------ misc helpers

  @Test
  public void supportsLocalServiceDefaultsToFalse() {
    ServiceCombServer s = server(instance("p1", SELF_HOST));
    Assertions.assertFalse(filter.supportsLocalService(s));

    Map<String, String> properties = new HashMap<>();
    properties.put("supportLocalService", "true");
    Assertions.assertTrue(filter.supportsLocalService(server(instance("p1", SELF_HOST, properties))));
  }

  @Test
  public void localInstanceDetectionUsesNonLoopbackAddresses() {
    ServiceCombServer local = server("rest://" + SELF_HOST + ":8080", instance("p1", SELF_HOST));
    Assertions.assertTrue(filter.isLocalInstance(local));

    ServiceCombServer loopback = server("rest://127.0.0.1:8080", instance("p1", "127.0.0.1"));
    Assertions.assertFalse(filter.isLocalInstance(loopback));

    Assertions.assertFalse(filter.isLocalInstance(null));
  }

  @Test
  public void emptyServerInstanceIdsMakeCacheInvalid() {
    LocalServicePriorityFilter.InstanceWeights weights = new LocalServicePriorityFilter.InstanceWeights();
    Assertions.assertFalse(filter.isCacheValid(weights, Collections.emptyList()));
  }

  public static final class StubInstance implements DiscoveryInstance {
    private final String instanceId;

    private final String hostOrIp;

    private final Map<String, String> properties;

    public StubInstance(String instanceId, String hostOrIp, Map<String, String> properties) {
      this.instanceId = instanceId;
      this.hostOrIp = hostOrIp;
      this.properties = properties == null ? new HashMap<>() : new HashMap<>(properties);
    }

    @Override
    public String getEnvironment() {
      return "";
    }

    @Override
    public String getApplication() {
      return "app";
    }

    @Override
    public String getServiceName() {
      return "provider";
    }

    @Override
    public String getAlias() {
      return "";
    }

    @Override
    public String getVersion() {
      return "1.0.0";
    }

    @Override
    public DataCenterInfo getDataCenterInfo() {
      return null;
    }

    @Override
    public String getDescription() {
      return "";
    }

    @Override
    public Map<String, String> getProperties() {
      return properties;
    }

    @Override
    public Map<String, String> getSchemas() {
      return new HashMap<>();
    }

    @Override
    public List<String> getEndpoints() {
      return List.of("rest://" + hostOrIp + ":8080");
    }

    @Override
    public String getInstanceId() {
      return instanceId;
    }

    @Override
    public MicroserviceInstanceStatus getStatus() {
      return MicroserviceInstanceStatus.UP;
    }

    @Override
    public String getRegistryName() {
      return "test";
    }
  }
}
