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

import org.apache.servicecomb.provider.common.MockUtil;
import org.apache.servicecomb.provider.pojo.PojoConst;
import org.junit.Assert;
import org.junit.Test;

public class TestSpringInstanceFactory {

  @Test
  public void testInitException()
      throws Exception {

    SpringInstanceFactory lSpringInstanceFactory = new SpringInstanceFactory();
    MockUtil.getInstance().mockBeanUtils();
    try {
      lSpringInstanceFactory.create("TestSpringInstanceFactory");
    } catch (Error e) {
      Assert.assertEquals("Fail to find bean:TestSpringInstanceFactory", e.getMessage());
    }
  }

  @Test
  public void testInit()
      throws Exception {

    SpringInstanceFactory lSpringInstanceFactory = new SpringInstanceFactory();
    MockUtil.getInstance().mockBeanUtils();
    MockUtil.getInstance().mockBeanUtilsObject();
    lSpringInstanceFactory.create("org.apache.servicecomb.provider.pojo.instance.TestPojoInstanceFactory");
    Assert.assertEquals(PojoConst.SPRING, lSpringInstanceFactory.getImplName());
  }
}
