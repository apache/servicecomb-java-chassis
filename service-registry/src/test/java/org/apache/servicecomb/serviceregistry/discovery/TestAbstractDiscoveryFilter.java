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

package org.apache.servicecomb.serviceregistry.discovery;

import org.junit.Assert;
import org.junit.Test;

public class TestAbstractDiscoveryFilter {
  int initCallCount;

  int childrenInitedCallCount;

  boolean[] inited = new boolean[] {true, true};

  DiscoveryContext context = new DiscoveryContext();

  DiscoveryTreeNode child = new DiscoveryTreeNode().name("c1");

  DiscoveryTreeNode parent = new DiscoveryTreeNode() {
    public boolean childrenInited() {
      childrenInitedCallCount++;
      return inited[childrenInitedCallCount - 1];
    }
  };

  class AbstractDiscoveryFilterForTest extends AbstractDiscoveryFilter {
    @Override
    public int getOrder() {
      return 0;
    }

    @Override
    protected void init(DiscoveryContext context, DiscoveryTreeNode parent) {
      initCallCount++;
    }

    @Override
    protected String findChildName(DiscoveryContext context, DiscoveryTreeNode parent) {
      return child.name();
    }
  }

  AbstractDiscoveryFilterForTest filter = new AbstractDiscoveryFilterForTest();

  DiscoveryTreeNode result;

  private void doDiscovery() {
    parent.child(child.name(), child);
    result = filter.discovery(context, parent);
  }

  @Test
  public void discoveryInited() {
    doDiscovery();

    Assert.assertEquals(1, childrenInitedCallCount);
    Assert.assertEquals(0, initCallCount);
    Assert.assertSame(child, result);
  }



  @Test
  public void discoveryNotInitedOnce() {
    inited[0] = false;
    doDiscovery();

    Assert.assertEquals(2, childrenInitedCallCount);
    Assert.assertEquals(0, initCallCount);
    Assert.assertSame(child, result);
  }

  @Test
  public void discoveryNotInitedTwice() {
    inited[0] = false;
    inited[1] = false;
    doDiscovery();

    Assert.assertEquals(2, childrenInitedCallCount);
    Assert.assertEquals(1, initCallCount);
    Assert.assertSame(child, result);
  }
}
