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

package org.apache.servicecomb.config.client;

import org.apache.servicecomb.config.ConfigUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ApolloConfigTest {
  @BeforeAll
  public static void setUpClass() {
    ApolloConfig.setConcurrentCompositeConfiguration(ConfigUtil.createLocalConfig());
  }

  @Test
  public void getServiceName() {
    ApolloConfig instance = ApolloConfig.INSTANCE;
    Assertions.assertEquals("apollo-test", instance.getServiceName());
    Assertions.assertEquals("http://127.0.0.1:8070", instance.getServerUri());
    Assertions.assertEquals("DEV", instance.getEnv());
    Assertions.assertEquals("test-cluster", instance.getServerClusters());
    Assertions.assertEquals("application", instance.getNamespace());
    Assertions.assertEquals("xxx", instance.getToken());
    Assertions.assertEquals(30, instance.getRefreshInterval());
    Assertions.assertEquals(0, instance.getFirstRefreshInterval());
  }
}
