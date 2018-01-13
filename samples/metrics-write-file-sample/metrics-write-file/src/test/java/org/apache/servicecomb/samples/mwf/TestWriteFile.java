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

package org.apache.servicecomb.samples.mwf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.metrics.common.CallMetric;
import org.apache.servicecomb.metrics.common.ConsumerInvocationMetric;
import org.apache.servicecomb.metrics.common.RegistryMetric;
import org.apache.servicecomb.metrics.common.SystemMetric;
import org.apache.servicecomb.metrics.common.TimerMetric;
import org.apache.servicecomb.metrics.core.publish.DataSource;
import org.apache.servicecomb.serviceregistry.Features;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.InstanceCacheManager;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.consumer.AppManager;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import mockit.Expectations;

public class TestWriteFile {

  @Test
  public void test() {

    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getServiceRegistry();
        result = new ServiceRegistry() {
          @Override
          public void init() {

          }

          @Override
          public void run() {

          }

          @Override
          public void destroy() {

          }

          @Override
          public Set<String> getCombinedMicroserviceNames() {
            return null;
          }

          @Override
          public Microservice getMicroservice() {
            return null;
          }

          @Override
          public MicroserviceInstance getMicroserviceInstance() {
            return null;
          }

          @Override
          public ServiceRegistryClient getServiceRegistryClient() {
            return null;
          }

          @Override
          public AppManager getAppManager() {
            return null;
          }

          @Override
          public InstanceCacheManager getInstanceCacheManager() {
            return null;
          }

          @Override
          public List<MicroserviceInstance> findServiceInstance(String appId, String microserviceName,
              String microserviceVersionRule) {
            return null;
          }

          @Override
          public MicroserviceInstances findServiceInstances(String appId, String microserviceName,
              String microserviceVersionRule, String revision) {
            return null;
          }

          @Override
          public boolean updateMicroserviceProperties(Map<String, String> properties) {
            return false;
          }

          @Override
          public boolean updateInstanceProperties(Map<String, String> instanceProperties) {
            return false;
          }

          @Override
          public Microservice getRemoteMicroservice(String microserviceId) {
            return null;
          }

          @Override
          public Features getFeatures() {
            return null;
          }
        };
      }
    };

    StringBuilder builder = new StringBuilder();

    MetricsFileWriter writer =
        (loggerName, filePrefix, content) -> builder.append(loggerName).append(filePrefix).append(content);

    SystemMetric systemMetric = new SystemMetric(50, 10, 1, 2, 3,
        4, 5, 6, 7, 8);

    Map<String, ConsumerInvocationMetric> consumerInvocationMetricMap = new HashMap<>();
    consumerInvocationMetricMap.put("A",
        new ConsumerInvocationMetric("A", "A",
            new TimerMetric("A1", 1, 2, 3, 4), new CallMetric("A2", 100, 999.44444)));

    consumerInvocationMetricMap.put("B",
        new ConsumerInvocationMetric("B", "B",
            new TimerMetric("B1", 1, 2, 3, 4), new CallMetric("B2", 100, 888.66666)));

    RegistryMetric metric = new RegistryMetric(systemMetric, consumerInvocationMetricMap, new HashMap<>());

    DataSource dataSource = Mockito.mock(DataSource.class);
    Mockito.when(dataSource.getRegistryMetric()).thenReturn(metric);

    WriteFileInitializer writeFileInitializer = new WriteFileInitializer(writer, dataSource,
        "localhost", "appId.serviceName");

    writeFileInitializer.run();

    String sb = builder.toString();

    Assert.assertTrue(sb.contains("999.4"));
    Assert.assertTrue(sb.contains("888.7"));
  }
}
