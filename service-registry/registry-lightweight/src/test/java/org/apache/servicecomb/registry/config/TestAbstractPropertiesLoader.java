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

package org.apache.servicecomb.registry.config;

import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.registry.lightweight.model.AbstractPropertiesLoader;
import org.apache.servicecomb.registry.lightweight.model.MicroservicePropertiesLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class TestAbstractPropertiesLoader {
  @Test
  public void testExtendedClassCompatible() {
    Environment environment = Mockito.mock(Environment.class);
    Mockito.when(environment.getProperty(BootStrapProperties.CONFIG_SERVICE_EXTENDED_CLASS))
        .thenReturn("invalidClass");
    AbstractPropertiesLoader loader = MicroservicePropertiesLoader.INSTANCE;
    try {
      loader.loadProperties(environment);
      Assertions.fail("Must throw exception");
    } catch (Error e) {
      Assertions.assertEquals(ClassNotFoundException.class, e.getCause().getClass());
      Assertions.assertEquals("invalidClass", e.getCause().getMessage());
    }
  }
}
