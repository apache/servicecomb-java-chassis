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

package org.apache.servicecomb.provider.pojo.instance;

import org.apache.servicecomb.provider.pojo.PojoConst;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPojoInstanceFactory {

  @Test
  public void testInitException() {
    PojoInstanceFactory lPojoInstanceFactory = new PojoInstanceFactory();
    try {
      lPojoInstanceFactory.create("TestPojoInstanceFactory");
    } catch (Error e) {
      Assertions.assertEquals("Fail to create instance of class:TestPojoInstanceFactory", e.getMessage());
    }
  }

  @Test
  public void testInit() {
    PojoInstanceFactory lPojoInstanceFactory = new PojoInstanceFactory();
    lPojoInstanceFactory.create("org.apache.servicecomb.provider.pojo.instance.TestPojoInstanceFactory");
    Assertions.assertEquals(PojoConst.POJO, lPojoInstanceFactory.getImplName());
  }
}
