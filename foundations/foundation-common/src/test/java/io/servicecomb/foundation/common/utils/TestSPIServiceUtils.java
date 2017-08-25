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

package io.servicecomb.foundation.common.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test SPIServiceUtils 
 *
 *
 */
public class TestSPIServiceUtils {
  @Test
  public void testGetTargetServiceNull() {
    SPIServiceDef0 service = SPIServiceUtils.getTargetService(SPIServiceDef0.class);
    Assert.assertNull(service);
  }

  @Test
  public void testGetTargetServiceNotNull() {
    SPIServiceDef service = SPIServiceUtils.getTargetService(SPIServiceDef.class);
    Assert.assertTrue(SPIServiceDef.class.isInstance(service));
  }
}
