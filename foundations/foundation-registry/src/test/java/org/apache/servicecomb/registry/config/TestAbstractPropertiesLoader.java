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

import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_SERVICE_DESCRIPTION_KEY;

import org.apache.commons.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

import com.netflix.config.DynamicConfiguration;

public class TestAbstractPropertiesLoader {
  @Test
  public void testMergeStrings() {
    Assert.assertEquals("abc123efg", AbstractPropertiesLoader.mergeStrings("abc", "123", "efg"));
  }

  @Test
  public void testExtendedClassCompatible() {
    Configuration configuration = new DynamicConfiguration();
    configuration.setProperty(CONFIG_SERVICE_DESCRIPTION_KEY + AbstractPropertiesLoader.EXTENDED_CLASS, "invalidClass");

    AbstractPropertiesLoader loader = MicroservicePropertiesLoader.INSTANCE;
    try {
      loader.loadProperties(configuration);
      Assert.fail("Must throw exception");
    } catch (Error e) {
      Assert.assertEquals(ClassNotFoundException.class, e.getCause().getClass());
      Assert.assertEquals("invalidClass", e.getCause().getMessage());
    }
  }
}
