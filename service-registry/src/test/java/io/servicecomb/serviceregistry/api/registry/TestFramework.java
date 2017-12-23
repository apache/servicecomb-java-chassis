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

package io.servicecomb.serviceregistry.api.registry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestFramework {

  Framework framework = null;

  @Before
  public void setUp() throws Exception {
    framework = new Framework();
  }

  @After
  public void tearDown() throws Exception {
    framework = null;
  }

  @Test
  public void testDefaultValues() {
    Assert.assertNull(framework.getName());
    Assert.assertNull(framework.getVersion());
  }

  @Test
  public void testInitializedValues() {
    initFramework(); //Initialize the Values
    Assert.assertEquals("JAVA-CHASSIS", framework.getName());
    Assert.assertEquals("0.6.0", framework.getVersion());
  }

  private void initFramework() {
    framework.setName("JAVA-CHASSIS");
    framework.setVersion("0.6.0");
  }
}
