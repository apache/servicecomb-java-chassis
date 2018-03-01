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

package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher;

import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;
import org.junit.Assert;
import org.junit.Test;

public class SimpleItemMatcherTest {

  private static final SimpleItemMatcher MATCHER = new SimpleItemMatcher();

  public static final String PATTERN = "%h - - %t %r %s %B";

  @Test
  public void testMatch() {
    AccessLogItemLocation location = MATCHER.match(PATTERN, 0);
    Assert.assertEquals(
        location,
        new AccessLogItemLocation()
            .setStart(0)
            .setEnd(2)
            .setPlaceHolder(AccessLogItemTypeEnum.REMOTE_HOSTNAME));

    location = MATCHER.match(PATTERN, 3);
    Assert.assertEquals(
        location,
        new AccessLogItemLocation()
            .setStart(7)
            .setEnd(9)
            .setPlaceHolder(AccessLogItemTypeEnum.DATETIME_DEFAULT));

    location = MATCHER.match(PATTERN, 17);
    Assert.assertNull(location);
  }

  @Test
  public void testNotMatch() {
    AccessLogItemLocation location = MATCHER.match("notmatch", 0);
    Assert.assertNull(location);
  }
}
