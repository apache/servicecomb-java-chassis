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

package org.apache.servicecomb.demo.pojo.test;

import static org.apache.servicecomb.serviceregistry.client.LocalServiceRegistryClientImpl.LOCAL_REGISTRY_FILE_KEY;
import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class PojoSpringConnectionLimitIntegrationTest {
  @BeforeClass
  public static void setUpClass() throws Exception {
    System.setProperty(LOCAL_REGISTRY_FILE_KEY, "notExistJustForceLocal");
    PojoTestMain.main(null);
  }

  @Test
  public void remoteHelloPojo_sayHello() {
    try {
      PojoService.hello.SayHello("whatever");
      fail("connection limit failed");
    } catch (Exception e) {
      Assert.assertEquals("java.io.IOException: socket closed", e.getCause().toString());
    }
  }
}