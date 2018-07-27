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

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class PojoSpringIntegrationTest extends PojoIntegrationTestBase {

  private static final ConnectionEventWatcher watcher = new ConnectionEventWatcher();

  @BeforeClass
  public static void setUpClass() throws Exception {
    setUpLocalRegistry();
    EventManager.register(watcher);
    PojoTestMain.main(null);
  }

  @AfterClass
  public static void teardownClass() {
    SCBEngine.getInstance().destroy();
    Assert.assertArrayEquals("check connection count change", new Integer[] {1, 0}, watcher.getCounters().toArray());
  }
}
