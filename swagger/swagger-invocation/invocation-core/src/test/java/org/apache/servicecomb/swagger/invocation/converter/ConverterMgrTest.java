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

package org.apache.servicecomb.swagger.invocation.converter;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ConverterMgrTest {
  @Test
  public void isAssignable() {
    ConverterMgr converterMgr = new ConverterMgr();
    // simple type
    Assert.assertTrue(converterMgr.isAssignable(String.class, String.class));
    Assert.assertTrue(converterMgr.isAssignable(int.class, Integer.class));
    Assert.assertTrue(converterMgr.isAssignable(long.class, Long.class));
    Assert.assertTrue(converterMgr.isAssignable(void.class, Void.class));
    // simple type reverse
    Assert.assertTrue(converterMgr.isAssignable(String.class, String.class));
    Assert.assertTrue(converterMgr.isAssignable(Integer.class, int.class));
    Assert.assertTrue(converterMgr.isAssignable(Long.class, long.class));
    Assert.assertTrue(converterMgr.isAssignable(Void.class, void.class));
    // Object type
    Assert.assertTrue(converterMgr.isAssignable(ArrayList.class, List.class));
    Assert.assertFalse(converterMgr.isAssignable(List.class, ArrayList.class));
  }
}
