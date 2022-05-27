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

package org.apache.servicecomb.bizkeeper;

import org.apache.servicecomb.core.Invocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestCustomCommandGroupKey {
  @Test
  public void testToHystrixCommandGroupKey() {
    Invocation invocation = Mockito.mock(Invocation.class);
    CustomizeCommandGroupKey customizeCommandGroupKey =
        (CustomizeCommandGroupKey) CustomizeCommandGroupKey.asKey("key", invocation);
    Assertions.assertEquals(invocation, customizeCommandGroupKey.getInstance());
  }

  @Test
  public void testOOM() {
    Invocation invocation1 = Mockito.mock(Invocation.class);
    Invocation invocation2 = Mockito.mock(Invocation.class);
    CustomizeCommandGroupKey customizeCommandGroupKey1 =
        (CustomizeCommandGroupKey) CustomizeCommandGroupKey.asKey("key", invocation1);
    CustomizeCommandGroupKey customizeCommandGroupKey2 =
        (CustomizeCommandGroupKey) CustomizeCommandGroupKey.asKey("key", invocation2);
    Assertions.assertEquals(customizeCommandGroupKey1, customizeCommandGroupKey2);
  }
}
