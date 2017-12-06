/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.springboot.starter.registry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mockit.Tested;

public class TestSpringCloudTransport {

  @Tested
  private SpringCloudTransport springCloudTransport;

  @Before
  public void setUp() throws Exception {
    springCloudTransport = new SpringCloudTransport();
  }

  @After
  public void tearDown() throws Exception {
    springCloudTransport = null;
  }

  @Test
  public void testSpringCloudTransport() throws Exception {
    Assert.assertEquals("rest", springCloudTransport.getName());
    Assert.assertEquals(-1000, springCloudTransport.getOrder());
    springCloudTransport.send(null, null);
    Assert.assertEquals(true, springCloudTransport.canInit());
    Assert.assertEquals(true, springCloudTransport.init());
  }
}
