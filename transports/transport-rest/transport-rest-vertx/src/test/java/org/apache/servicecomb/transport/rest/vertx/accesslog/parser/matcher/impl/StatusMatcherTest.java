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

package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import static org.junit.Assert.assertEquals;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.StatusElement;
import org.junit.Test;

public class StatusMatcherTest {
  private static final StatusMatcher MATCHER = new StatusMatcher();

  @Test
  public void getPlaceholderPatterns() {
    String[] patterns = MATCHER.getPlaceholderPatterns();
    assertEquals(2, patterns.length);
    assertEquals("%s", patterns[0]);
    assertEquals("cs-status", patterns[1]);
  }

  @Test
  public void getAccessLogElement() {
    assertEquals(StatusElement.class, MATCHER.getAccessLogElement().getClass());
  }
}
