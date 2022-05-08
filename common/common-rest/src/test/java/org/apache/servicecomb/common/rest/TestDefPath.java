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

package org.apache.servicecomb.common.rest;

import org.apache.servicecomb.common.rest.definition.path.PathRegExp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestDefPath {

  @Test
  public void testPathRegExp() throws Exception {
    PathRegExp oPathRegExp = new PathRegExp("//{test}//");
    Assertions.assertEquals(1, oPathRegExp.getGroupCount());
    Assertions.assertEquals(0, oPathRegExp.getGroupWithRegExpCount());
    PathRegExp oSecondPathRegExp = new PathRegExp("{[^/:]+?}");
    Assertions.assertEquals(1, oSecondPathRegExp.getGroupCount());
    Assertions.assertEquals(1, oSecondPathRegExp.getGroupWithRegExpCount());
    Assertions.assertEquals("test/", PathRegExp.ensureEndWithSlash("test/"));
    Assertions.assertEquals("test/", PathRegExp.ensureEndWithSlash("test"));
    Assertions.assertNull(oSecondPathRegExp.match("{test/test}", null));
  }
}
