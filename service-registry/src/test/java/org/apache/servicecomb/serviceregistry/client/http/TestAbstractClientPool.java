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
package org.apache.servicecomb.serviceregistry.client.http;

import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.junit.After;
import org.junit.Before;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestAbstractClientPool {
  @Mocked
  Vertx vertx;

  String vertxName;

  VertxOptions vertxOptions;

  Class<?> verticleCls;

  DeploymentOptions deployOptions;

  @Before
  public void setup() {
    new MockUp<VertxUtils>() {
      @Mock
      Vertx getOrCreateVertxByName(String name, VertxOptions vertxOptions) {
        TestAbstractClientPool.this.vertxName = name;
        TestAbstractClientPool.this.vertxOptions = vertxOptions;

        return vertx;
      }

      @Mock
      <VERTICLE extends AbstractVerticle> boolean blockDeploy(Vertx vertx,
          Class<VERTICLE> cls,
          DeploymentOptions options) {
        TestAbstractClientPool.this.verticleCls = cls;
        TestAbstractClientPool.this.deployOptions = options;

        return true;
      }
    };
    ArchaiusUtils.resetConfig();
  }

  @After
  public void teardown() {
    ArchaiusUtils.resetConfig();
  }
}
