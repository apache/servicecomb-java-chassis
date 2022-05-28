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

package org.apache.servicecomb.serviceregistry.registry.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MicroserviceCacheKeyTest {

  @Test
  public void constructors() {
    checkConstructorException(null, "appId", "svc", "microserviceCacheKey.env is null");
    checkConstructorException("env", null, "svc", "microserviceCacheKey.appId is null");
    checkConstructorException("env", "appId", null, "microserviceCacheKey.serviceName is null");

    MicroserviceCacheKey microserviceCacheKey =
        MicroserviceCacheKey.builder().serviceName("svc").appId("app").env("env").build();
    Assertions.assertEquals("svc", microserviceCacheKey.getServiceName());
    Assertions.assertEquals("app", microserviceCacheKey.getAppId());
    Assertions.assertEquals("env", microserviceCacheKey.getEnv());
    Assertions.assertEquals("svc@app@env@0.0.0.0+", microserviceCacheKey.toString());

    microserviceCacheKey =
        MicroserviceCacheKey.builder().serviceName("app:svc").appId("app").env("env").build();
    Assertions.assertEquals("svc", microserviceCacheKey.getServiceName());
    Assertions.assertEquals("app", microserviceCacheKey.getAppId());
    Assertions.assertEquals("env", microserviceCacheKey.getEnv());

    microserviceCacheKey =
        MicroserviceCacheKey.builder().serviceName("app2:svc").appId("app").env("env").build();
    Assertions.assertEquals("svc", microserviceCacheKey.getServiceName());
    Assertions.assertEquals("app2", microserviceCacheKey.getAppId());
    Assertions.assertEquals("env", microserviceCacheKey.getEnv());
  }

  private void checkConstructorException(String env, String appId, String svc, String expectedMessage) {
    try {
      MicroserviceCacheKey.builder().env(env).appId(appId).serviceName(svc).build();
      Assertions.fail("an Exception is expected!");
    } catch (Exception e) {
      Assertions.assertEquals(expectedMessage, e.getMessage());
    }
  }

  @Test
  public void equals_and_hashcode() {
    MicroserviceCacheKey microserviceCacheKey =
        MicroserviceCacheKey.builder().env("env").appId("app").serviceName("svc").build();
    MicroserviceCacheKey microserviceCacheKey2 =
        MicroserviceCacheKey.builder().env("env").appId("app").serviceName("svc").build();
    Assertions.assertEquals(microserviceCacheKey, microserviceCacheKey2);
    Assertions.assertEquals(microserviceCacheKey.hashCode(), microserviceCacheKey2.hashCode());

    microserviceCacheKey2 =
        MicroserviceCacheKey.builder().env("env1").appId("app").serviceName("svc").build();
    Assertions.assertNotEquals(microserviceCacheKey, microserviceCacheKey2);
    microserviceCacheKey2 =
        MicroserviceCacheKey.builder().env("env").appId("app1").serviceName("svc").build();
    Assertions.assertNotEquals(microserviceCacheKey, microserviceCacheKey2);
    microserviceCacheKey2 =
        MicroserviceCacheKey.builder().env("env").appId("app").serviceName("svc1").build();
    Assertions.assertNotEquals(microserviceCacheKey, microserviceCacheKey2);
  }

  @Test
  public void plainKey() {
    MicroserviceCacheKey microserviceCacheKey =
        MicroserviceCacheKey.builder().env("env").appId("app").serviceName("svc").build();
    Assertions.assertEquals("svc@app@env@0.0.0.0+", microserviceCacheKey.plainKey());
  }
}
