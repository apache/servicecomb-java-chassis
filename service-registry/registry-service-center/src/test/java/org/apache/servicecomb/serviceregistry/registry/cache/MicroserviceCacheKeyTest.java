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

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

public class MicroserviceCacheKeyTest {

  @Test
  public void constructors() {
    checkConstructorException(null, "appId", "svc", "microserviceCacheKey.env is null");
    checkConstructorException("env", null, "svc", "microserviceCacheKey.appId is null");
    checkConstructorException("env", "appId", null, "microserviceCacheKey.serviceName is null");

    MicroserviceCacheKey microserviceCacheKey =
        MicroserviceCacheKey.builder().serviceName("svc").appId("app").env("env").build();
    Assert.assertEquals("svc", microserviceCacheKey.getServiceName());
    Assert.assertEquals("app", microserviceCacheKey.getAppId());
    Assert.assertEquals("env", microserviceCacheKey.getEnv());
    Assert.assertEquals("svc@app@env", microserviceCacheKey.toString());

    microserviceCacheKey =
        MicroserviceCacheKey.builder().serviceName("app:svc").appId("app").env("env").build();
    Assert.assertEquals("svc", microserviceCacheKey.getServiceName());
    Assert.assertEquals("app", microserviceCacheKey.getAppId());
    Assert.assertEquals("env", microserviceCacheKey.getEnv());

    microserviceCacheKey =
        MicroserviceCacheKey.builder().serviceName("app2:svc").appId("app").env("env").build();
    Assert.assertEquals("svc", microserviceCacheKey.getServiceName());
    Assert.assertEquals("app2", microserviceCacheKey.getAppId());
    Assert.assertEquals("env", microserviceCacheKey.getEnv());
  }

  private void checkConstructorException(String env, String appId, String svc, String expectedMessage) {
    try {
      MicroserviceCacheKey.builder().env(env).appId(appId).serviceName(svc).build();
      fail("an Exception is expected!");
    } catch (Exception e) {
      Assert.assertEquals(expectedMessage, e.getMessage());
    }
  }

  @Test
  public void equals_and_hashcode() {
    MicroserviceCacheKey microserviceCacheKey =
        MicroserviceCacheKey.builder().env("env").appId("app").serviceName("svc").build();
    MicroserviceCacheKey microserviceCacheKey2 =
        MicroserviceCacheKey.builder().env("env").appId("app").serviceName("svc").build();
    Assert.assertEquals(microserviceCacheKey, microserviceCacheKey2);
    Assert.assertEquals(microserviceCacheKey.hashCode(), microserviceCacheKey2.hashCode());

    microserviceCacheKey2 =
        MicroserviceCacheKey.builder().env("env1").appId("app").serviceName("svc").build();
    Assert.assertNotEquals(microserviceCacheKey, microserviceCacheKey2);
    microserviceCacheKey2 =
        MicroserviceCacheKey.builder().env("env").appId("app1").serviceName("svc").build();
    Assert.assertNotEquals(microserviceCacheKey, microserviceCacheKey2);
    microserviceCacheKey2 =
        MicroserviceCacheKey.builder().env("env").appId("app").serviceName("svc1").build();
    Assert.assertNotEquals(microserviceCacheKey, microserviceCacheKey2);
  }

  @Test
  public void plainKey() {
    MicroserviceCacheKey microserviceCacheKey =
        MicroserviceCacheKey.builder().env("env").appId("app").serviceName("svc").build();
    Assert.assertEquals("svc@app@env", microserviceCacheKey.plainKey());
  }
}