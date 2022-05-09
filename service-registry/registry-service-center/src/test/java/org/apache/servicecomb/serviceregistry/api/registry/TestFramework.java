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

package org.apache.servicecomb.serviceregistry.api.registry;

import org.apache.servicecomb.registry.api.registry.Framework;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestFramework {
  @Test
  public void testDefaultValues() {
    Framework framework = new Framework();
    Assertions.assertNull(framework.getName());
    Assertions.assertNull(framework.getVersion());
  }

  @Test
  public void testInitializedValues() {
    Framework framework = new Framework();
    framework.setName("JAVA-CHASSIS");
    framework.setVersion("x.x.x");
    Assertions.assertEquals("JAVA-CHASSIS", framework.getName());
    Assertions.assertEquals("x.x.x", framework.getVersion());
  }
}
