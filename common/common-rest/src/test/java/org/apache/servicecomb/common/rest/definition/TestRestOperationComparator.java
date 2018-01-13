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

package org.apache.servicecomb.common.rest.definition;

import org.apache.servicecomb.common.rest.locator.MicroservicePaths;
import org.junit.Assert;
import org.junit.Test;

public class TestRestOperationComparator {
  @Test
  public void testStaticCharCount() {
    RestOperationMeta less = new RestOperationMeta();
    less.setAbsolutePath("/a/{id}");

    RestOperationMeta more = new RestOperationMeta();
    more.setAbsolutePath("/abc/{id}");

    MicroservicePaths paths = new MicroservicePaths();
    paths.addResource(less);
    paths.addResource(more);
    paths.sortPath();

    Assert.assertSame(more, paths.getDynamicPathOperationList().get(0));
    Assert.assertSame(less, paths.getDynamicPathOperationList().get(1));
  }

  @Test
  public void testVarGroupCount() {
    RestOperationMeta less = new RestOperationMeta();
    less.setAbsolutePath("/ab/{id}");

    RestOperationMeta more = new RestOperationMeta();
    more.setAbsolutePath("/a/{test}/{id}");

    MicroservicePaths paths = new MicroservicePaths();
    paths.addResource(less);
    paths.addResource(more);
    paths.sortPath();

    Assert.assertSame(more, paths.getDynamicPathOperationList().get(0));
    Assert.assertSame(less, paths.getDynamicPathOperationList().get(1));
  }

  @Test
  public void testGroupWithRegExpCount() {
    RestOperationMeta less = new RestOperationMeta();
    less.setAbsolutePath("/a/{test}/{id}");

    RestOperationMeta more = new RestOperationMeta();
    more.setAbsolutePath("/a/{test : .+}/{id}");

    MicroservicePaths paths = new MicroservicePaths();
    paths.addResource(less);
    paths.addResource(more);
    paths.sortPath();

    Assert.assertSame(more, paths.getDynamicPathOperationList().get(0));
    Assert.assertSame(less, paths.getDynamicPathOperationList().get(1));
  }
}
