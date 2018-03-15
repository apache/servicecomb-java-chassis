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
package org.apache.servicecomb.foundation.metrics.publish.spectator;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestTagFinder {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void buildFromString() {
    String name = "key";
    TagFinder finder = TagFinder.build(name);

    Assert.assertEquals(name, finder.getTagKey());
    Assert.assertEquals(DefaultTagFinder.class, finder.getClass());
  }

  @Test
  public void buildFromTagFinder() {
    TagFinder finder = new DefaultTagFinder("key");
    Assert.assertSame(finder, TagFinder.build(finder));
  }

  @Test
  public void buildFromInvalidType() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException
        .expectMessage(Matchers.is("only support String or TagFinder, but got " + Integer.class.getName()));

    TagFinder.build(1);
  }

  @Test
  public void buildFromNull() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException
        .expectMessage(Matchers.is("only support String or TagFinder, but got null"));

    TagFinder.build(null);
  }
}
