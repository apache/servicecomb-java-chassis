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

package org.apache.servicecomb.demo.edge.consumer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestOperations;

@Component
public class EdgeServiceGovernanceTest implements CategorizedTestCase {
  RestOperations template = RestTemplateBuilder.create();

  String edgePrefix;

  @Autowired
  Environment environment;

  @Autowired
  DiscoveryManager discoveryManager;

  @Override
  public void testRestTransport() throws Exception {
    prepareEdge("url");
    // edge service do not support retry
//    testEdgeServiceRetry();

    testEdgeServiceInstanceIsolation(); // may isolate instance for 5 seconds.
    Thread.sleep(6000); // ensure isolation is open for new requests
    testEdgeServiceInstanceBulkhead();
  }

  private void testEdgeServiceInstanceBulkhead() throws Exception {
    String url = edgePrefix + "/business/v2/testEdgeServiceInstanceBulkhead";

    CountDownLatch latch = new CountDownLatch(100);
    AtomicBoolean expectedFailed503 = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              String result = template.getForObject(url + "?name={1}", String.class, "hello");
              if (!"hello".equals(result)) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              if (!(e instanceof HttpServerErrorException)) {
                notExpectedFailed.set(true);
              } else {
                if (((HttpServerErrorException) e).getStatusCode().value() == 503) {
                  expectedFailed503.set(true);
                }
              }
            }
            latch.countDown();
          }
        }.start();
      }
      Thread.sleep(100);
    }

    latch.await(20, TimeUnit.SECONDS);
    TestMgr.check(true, expectedFailed503.get());
    TestMgr.check(false, notExpectedFailed.get());
  }

  private void testEdgeServiceInstanceIsolation() throws Exception {
    String url = edgePrefix + "/business/v2/testEdgeServiceInstanceIsolation";

    CountDownLatch latch = new CountDownLatch(100);
    AtomicBoolean expectedFailed404 = new AtomicBoolean(false);
    AtomicBoolean expectedFailed502 = new AtomicBoolean(false);
    AtomicBoolean expectedFailed503 = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              String result = template.getForObject(url + "?name={1}", String.class, "hello");
              if (!"hello".equals(result)) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              if (e instanceof HttpClientErrorException) {
                // isolate 2.0.0 and other instance will give 404
                if (((HttpClientErrorException) e).getStatusCode().value() == 404) {
                  expectedFailed404.set(true);
                } else {
                  notExpectedFailed.set(true);
                }
              } else if (e instanceof HttpServerErrorException) {
                // instance isolated and return 503
                if (((HttpServerErrorException) e).getStatusCode().value() == 503) {
                  expectedFailed503.set(true);
                }
                // provider throw 502 exception to trigger instance isolation
                if (((HttpServerErrorException) e).getStatusCode().value() == 502) {
                  expectedFailed502.set(true);
                }
              } else {
                notExpectedFailed.set(true);
              }
            }
            latch.countDown();
          }
        }.start();
      }
      Thread.sleep(100);
    }

    latch.await(20, TimeUnit.SECONDS);
    TestMgr.check(true, expectedFailed404.get());
    TestMgr.check(true, expectedFailed502.get());
    TestMgr.check(true, expectedFailed503.get());
    TestMgr.check(false, notExpectedFailed.get());
  }

  private URIEndpointObject prepareEdge(String prefix) {
    DiscoveryInstance microserviceInstance = discoveryManager.findServiceInstances(
            BootStrapProperties.readApplication(environment), "edge")
        .stream()
        .findFirst()
        .get();
    URIEndpointObject edgeAddress = new URIEndpointObject(microserviceInstance.getEndpoints().get(0));
    edgePrefix = String.format("http://%s:%d/%s", edgeAddress.getHostOrIp(), edgeAddress.getPort(), prefix);
    return edgeAddress;
  }
}
