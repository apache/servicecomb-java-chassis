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
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class TestCustomCommandGroupKey {
  @Test
  public void testToHystrixCommandGroupKey() {

    Invocation invocation = Mockito.mock(Invocation.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(Mockito.mock(OperationMeta.class));
    Mockito.when(invocation.getOperationMeta().getMicroserviceQualifiedName()).thenReturn("test2");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("microserviceName");
    Mockito.when(invocation.getInvocationType()).thenReturn(InvocationType.CONSUMER);
    Mockito.when(invocation.getSchemaId()).thenReturn("schemaId");
    Mockito.when(invocation.getOperationName()).thenReturn("operationName");
    CustomizeCommandGroupKey customizeCommandGroupKey =
        (CustomizeCommandGroupKey) CustomizeCommandGroupKey.asKey("type", invocation);
    Assert.assertEquals("CONSUMER", customizeCommandGroupKey.getInvocationType());
    Assert.assertEquals("microserviceName", customizeCommandGroupKey.getMicroserviceName());
    Assert.assertEquals("schemaId", customizeCommandGroupKey.getSchema());
    Assert.assertEquals("operationName", customizeCommandGroupKey.getOperation());
  }
}
