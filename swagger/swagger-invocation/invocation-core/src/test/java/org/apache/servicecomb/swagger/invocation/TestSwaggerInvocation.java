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
package org.apache.servicecomb.swagger.invocation;

import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestSwaggerInvocation {
  @Test
  public void construct_withContext() {
    InvocationContext parentContext = new InvocationContext();
    parentContext.addContext("k", "v");
    parentContext.addLocalContext("k", 1);
    ContextUtils.setInvocationContext(parentContext);

    try {
      SwaggerInvocation invocation = new SwaggerInvocation();
      Assertions.assertSame(parentContext, invocation.getParentContext());
      Assertions.assertEquals("v", invocation.getContext("k"));
      Assertions.assertEquals(1, (int) invocation.getLocalContext("k"));
    } finally {
      ContextUtils.removeInvocationContext();
    }
  }

  @Test
  public void construct_noContext() {
    SwaggerInvocation invocation = new SwaggerInvocation();
    Assertions.assertNull(invocation.getParentContext());
  }
}
