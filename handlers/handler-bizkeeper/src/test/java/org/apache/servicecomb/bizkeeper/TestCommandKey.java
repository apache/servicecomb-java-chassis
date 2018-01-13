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
import org.apache.servicecomb.core.definition.OperationMeta;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;

/**
 *
 *
 */
public class TestCommandKey {

  @Test
  public void testToHystrixCommandGroupKey() {
    Invocation invocation = Mockito.mock(Invocation.class);

    Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
    Mockito.when(invocation.getOperationMeta().getMicroserviceQualifiedName()).thenReturn("test1");
    Assert.assertNotNull(invocation);
    HystrixCommandGroupKey hystrixCommandGroupKey = CommandKey.toHystrixCommandGroupKey("groupname", invocation);
    Assert.assertNotNull(hystrixCommandGroupKey);
  }

  @Test
  public void testToHystrixCommandKey() {

    Invocation invocation = Mockito.mock(Invocation.class);

    Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
    Mockito.when(invocation.getOperationMeta().getMicroserviceQualifiedName()).thenReturn("test1");
    Assert.assertNotNull(invocation);
    HystrixCommandKey hystrixCommandGroupKey = CommandKey.toHystrixCommandKey("groupname", invocation);
    Assert.assertNotNull(hystrixCommandGroupKey);
  }
}
