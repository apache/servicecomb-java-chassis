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
package org.apache.servicecomb.huaweicloud.servicestage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestCasEnvConfig {

  @BeforeAll
  public static void init() {
    System.setProperty("CAS_APPLICATION_ID", "application-id");
    System.setProperty("CAS_ENVIRONMENT_ID", "env-id");
    System.setProperty("SERVICECOMB_SERVICE_PROPS", "component:ConsumerService,other:A");
    System.setProperty("SERVICECOMB_INSTANCE_PROPS", "route:gray");
  }

  @Test
  public void testConfig() {
    CasEnvConfig instance = CasEnvConfig.INSTANCE;
    Assertions.assertEquals(3, instance.getNonEmptyInstanceProperties().size());
    Assertions.assertEquals("application-id", instance.getNonEmptyInstanceProperties().get("CAS_APPLICATION_ID"));
    Assertions.assertEquals("env-id", instance.getNonEmptyInstanceProperties().get("CAS_ENVIRONMENT_ID"));
    Assertions.assertEquals("gray", instance.getNonEmptyInstanceProperties().get("route"));


    Assertions.assertEquals(2, instance.getNonEmptyServiceProperties().size());
    Assertions.assertEquals("ConsumerService", instance.getNonEmptyServiceProperties().get("component"));
    Assertions.assertEquals("A", instance.getNonEmptyServiceProperties().get("other"));
  }

  @AfterAll
  public static void destroy() {
    System.getProperties().remove("CAS_ENVIRONMENT_ID");
    System.getProperties().remove("CAS_APPLICATION_ID");
    System.getProperties().remove("SERVICECOMB_SERVICE_PROPS");
    System.getProperties().remove("SERVICECOMB_INSTANCE_PROPS");
  }
}
