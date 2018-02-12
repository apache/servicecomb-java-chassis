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

public class PercentagePrefixConfigurableMatcherTest {

  public static final PercentagePrefixConfigurableMatcher MATCHER = new PercentagePrefixConfigurableMatcher();

  public static final String TEST_RAW_PATTERN = "%{pattern}t %{test pattern}C %{test pattern}t";

  @Test
  public void testMatch() {
    AccessLogItemLocation location;
    location = MATCHER.match(TEST_RAW_PATTERN, 0);
    Assert.assertEquals(
        location,
        new AccessLogItemLocation()
            .setStart(0)
            .setEnd(11)
            .setPlaceHolder(AccessLogItemTypeEnum.DATETIME_CONFIGURABLE));

    location = MATCHER.match(TEST_RAW_PATTERN, 10);
    Assert.assertEquals(
        location,
        new AccessLogItemLocation()
            .setStart(12)
            .setEnd(28)
            .setPlaceHolder(AccessLogItemTypeEnum.COOKIE));

    location = MATCHER.match(TEST_RAW_PATTERN, 30);
    Assert.assertNull(location);
  }

  @Test
  public void testNotMatch() {
    AccessLogItemLocation location = MATCHER.match("notmatch", 0);
    Assert.assertNull(location);
  }

  @Test
  public void testNotMatchWithPrefix() {
    AccessLogItemLocation location = MATCHER.match("%{notmatch}x", 0);
    Assert.assertNull(location);
  }
}