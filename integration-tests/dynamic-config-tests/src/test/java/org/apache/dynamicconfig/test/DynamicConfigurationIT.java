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

package org.apache.dynamicconfig.test;

import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.netflix.config.DynamicPropertyFactory;

import io.vertx.core.Vertx;
import org.junit.jupiter.api.Assertions;

public class DynamicConfigurationIT {
  private static Vertx vertx = null;

  @BeforeAll
  public static void setUp() throws Exception {
    vertx = Vertx.vertx();
    vertx.deployVerticle(new SimApolloServer());

    Log4jUtils.init();
    BeanUtils.init();
  }

  @AfterAll
  public static void tearDown() {
    vertx.close();
  }

  @Test
  public void testDynamicConfiguration() {
    Assertions.assertEquals(6666, DynamicPropertyFactory.getInstance().getIntProperty("timeout", 0).get());
  }
}
