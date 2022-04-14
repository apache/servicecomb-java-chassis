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

package org.apache.servicecomb.governance;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.governance.handler.InstanceIsolationHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;

@SpringBootTest
@ContextConfiguration(locations = "classpath:META-INF/spring/*.xml", initializers = ConfigDataApplicationContextInitializer.class)
public class InstanceIsolationTest {
  private InstanceIsolationHandler instanceIsolationHandler;

  @Autowired
  public void setInstanceIsolationHandler(InstanceIsolationHandler instanceIsolationHandler) {
    this.instanceIsolationHandler = instanceIsolationHandler;
  }

  public InstanceIsolationTest() {
  }

  @Test
  public void test_instance_isolation_work() throws Throwable {
    AtomicInteger counter = new AtomicInteger(0);

    DecorateCheckedSupplier<Object> ds = Decorators.ofCheckedSupplier(() -> {
      int run = counter.getAndIncrement();
      if (run == 0) {
        return "test";
      }
      if (run == 1) {
        throw new RuntimeException("test exception");
      }
      return "test";
    });

    DecorateCheckedSupplier<Object> ds2 = Decorators.ofCheckedSupplier(() -> "test");

    GovernanceRequest request = new GovernanceRequest();
    request.setInstanceId("instance01");
    request.setServiceId("service01");

    CircuitBreaker circuitBreaker = instanceIsolationHandler.getActuator(request);
    ds.withCircuitBreaker(circuitBreaker);

    // isolation from error
    Assertions.assertEquals("test", ds.get());
    Assertions.assertThrows(RuntimeException.class, () -> ds.get());
    Assertions.assertThrows(RuntimeException.class, () -> ds.get());
    Assertions.assertThrows(RuntimeException.class, () -> ds.get());

    // isolation do not influence other instances
    GovernanceRequest request2 = new GovernanceRequest();
    request2.setInstanceId("instance02");
    request2.setServiceId("service01");

    CircuitBreaker circuitBreaker2 = instanceIsolationHandler.getActuator(request2);
    ds2.withCircuitBreaker(circuitBreaker2);

    Assertions.assertEquals("test", ds2.get());
    Assertions.assertEquals("test", ds2.get());
    Assertions.assertEquals("test", ds2.get());
    Assertions.assertEquals("test", ds2.get());

    // recover from isolation
    Thread.sleep(1000);

    Assertions.assertEquals("test", ds.get());
    Assertions.assertEquals("test", ds.get());
    Assertions.assertEquals("test", ds2.get());
    Assertions.assertEquals("test", ds2.get());
  }
}
