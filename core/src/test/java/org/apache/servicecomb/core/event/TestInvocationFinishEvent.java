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
package org.apache.servicecomb.core.event;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.Assert;
import org.junit.Test;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestInvocationFinishEvent {
  InvocationFinishEvent event;

  @Test
  public void construct(@Mocked Invocation invocation, @Mocked Response response) {
    long time = 123;
    new MockUp<System>() {
      @Mock
      long nanoTime() {
        return time;
      }
    };

    event = new InvocationFinishEvent(invocation, response);

    Assert.assertEquals(time, event.getNanoCurrent());
    Assert.assertSame(invocation, event.getInvocation());
    Assert.assertSame(response, event.getResponse());
  }
}
