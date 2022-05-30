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

package org.apache.servicecomb.common.rest.definition.path;

import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder.URLPathStringBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class URLPathStringBuilderTest {
  @Test
  public void testNormal() {
    URLPathStringBuilder builder = new URLPathStringBuilder();
    builder.appendPath("/path");
    builder.appendQuery("q", "abc");
    Assertions.assertEquals("/path?q=abc", builder.build());
  }

  @Test
  public void appendPath() {
    URLPathStringBuilder builder = new URLPathStringBuilder();
    builder.appendPath("/abc");
    Assertions.assertEquals("/abc", builder.build());
    builder.appendPath("/de fg");
    Assertions.assertEquals("/abc/de fg", builder.build());
  }

  @Test
  public void appendQuery() {
    URLPathStringBuilder builder = new URLPathStringBuilder();
    Assertions.assertEquals("", builder.build());
    builder.appendQuery("ab", "cd");
    Assertions.assertEquals("?ab=cd", builder.build());
    builder.appendQuery("ef", "");
    Assertions.assertEquals("?ab=cd&ef=", builder.build());
    builder.appendQuery("gh", "jk");
    Assertions.assertEquals("?ab=cd&ef=&gh=jk", builder.build());
  }
}
