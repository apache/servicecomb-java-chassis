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
package org.apache.servicecomb.swagger.invocation.context;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status.Family;

import org.junit.Assert;
import org.junit.Test;

public class TestInvocationContext {
  @Test
  public void addContext() {
    InvocationContext invocationContext = new InvocationContext();
    invocationContext.addContext("key1", "value1");
    Assert.assertEquals(1, invocationContext.getContext().size());
    Assert.assertEquals("value1", invocationContext.getContext("key1"));

    Map<String, String> otherContext = new HashMap<>();
    otherContext.put("key2", "value2");
    invocationContext.addContext(otherContext);
    Assert.assertEquals(2, invocationContext.getContext().size());
    Assert.assertEquals("value2", invocationContext.getContext("key2"));

    InvocationContext invocationContext2 = new InvocationContext();
    Map<String, String> otherContext2 = new HashMap<>();
    otherContext2.put("key3", "value3");
    invocationContext2.context = otherContext2;
    invocationContext.addContext(invocationContext2);
    Assert.assertEquals(3, invocationContext.getContext().size());
  }

  @Test
  public void mergeMapContext() {
    InvocationContext invocationContext = new InvocationContext();
    Map<String, String> otherContext = new HashMap<>();
    otherContext.put("key1", "value1");
    //otherContext's size is large than old.
    invocationContext.mergeContext(otherContext);
    Assert.assertEquals(1, invocationContext.getContext().size());
    Assert.assertEquals("value1", invocationContext.getContext("key1"));

    otherContext.put("key1", "value2");
    //otherContext's size is not large than old.
    invocationContext.mergeContext(otherContext);
    Assert.assertEquals(1, invocationContext.getContext().size());
    Assert.assertEquals("value2", invocationContext.getContext("key1"));
  }

  @Test
  public void mergeInvocationContext() {
    InvocationContext invocationContext = new InvocationContext();
    Map<String, String> otherContext = new HashMap<>();
    otherContext.put("key1", "value1");
    InvocationContext context2 = new InvocationContext();
    context2.context = otherContext;
    invocationContext.mergeContext(context2);
    Assert.assertEquals(1, invocationContext.getContext().size());
    Assert.assertEquals("value1", invocationContext.getContext("key1"));
  }

  @Test
  public void addLocalContext() {
    InvocationContext invocationContext = new InvocationContext();
    invocationContext.addLocalContext("key1", "value1");
    Assert.assertEquals(1, invocationContext.getLocalContext().size());
    Assert.assertEquals("value1", invocationContext.getLocalContext("key1"));

    Map<String, Object> otherContext = new HashMap<>();
    otherContext.put("key2", "value2");
    invocationContext.addLocalContext(otherContext);
    Assert.assertEquals(2, invocationContext.getLocalContext().size());
    Assert.assertEquals("value2", invocationContext.getLocalContext("key2"));
  }

  @Test
  public void setStatus() {
    InvocationContext invocationContext = new InvocationContext();
    invocationContext.setStatus(200);
    System.out.println(invocationContext.getStatus().getFamily());
    Assert.assertEquals(200, invocationContext.getStatus().getStatusCode());
    Assert.assertEquals("OK", invocationContext.getStatus().getReasonPhrase());
    Assert.assertEquals(Family.SUCCESSFUL, invocationContext.getStatus().getFamily());

    invocationContext.setStatus(200, "TEST");
    Assert.assertEquals(200, invocationContext.getStatus().getStatusCode());
    Assert.assertEquals("TEST", invocationContext.getStatus().getReasonPhrase());
    Assert.assertEquals(Family.SUCCESSFUL, invocationContext.getStatus().getFamily());
  }
}
