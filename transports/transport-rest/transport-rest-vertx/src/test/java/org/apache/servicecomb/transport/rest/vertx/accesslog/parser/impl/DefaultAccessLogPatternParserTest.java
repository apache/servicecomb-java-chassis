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

package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;
import org.hamcrest.Matchers;
import org.junit.Test;

import mockit.Deencapsulation;

public class DefaultAccessLogPatternParserTest {
  private static final String ROW_PATTERN = "[cs-method] %m %s%T%D%h%v%p%B%b%r%U%q"
      + "cs-uri-stemcs-uri-querycs-uri%H%t%{yyyy MM dd HH:mm:ss zzz}t"
      + "%{yyyy MM dd HH:mm:ss|GMT+0|en-US}t"
      + "%{incoming-header}i"
      + "%{outgoing-header}o"
      + "%{cookie}C"
      + "%SCB-traceId";

  private static DefaultAccessLogPatternParser accessLogPatternParser = new DefaultAccessLogPatternParser();

  @Test
  public void testParsePattern() {
    List<AccessLogItemLocation> result = accessLogPatternParser.parsePattern(ROW_PATTERN);
    assertEquals(27, result.size());

    assertThat(result.stream().map(AccessLogItemLocation::getPlaceHolder)
            .filter(Objects::nonNull).collect(Collectors.toList()),
        Matchers.contains(
            AccessLogItemTypeEnum.TEXT_PLAIN,
            AccessLogItemTypeEnum.HTTP_METHOD,
            AccessLogItemTypeEnum.TEXT_PLAIN,
            AccessLogItemTypeEnum.HTTP_METHOD,
            AccessLogItemTypeEnum.TEXT_PLAIN,
            AccessLogItemTypeEnum.HTTP_STATUS,
            AccessLogItemTypeEnum.DURATION_IN_SECOND,
            AccessLogItemTypeEnum.DURATION_IN_MILLISECOND,
            AccessLogItemTypeEnum.REMOTE_HOSTNAME,
            AccessLogItemTypeEnum.LOCAL_HOSTNAME,
            AccessLogItemTypeEnum.LOCAL_PORT,
            AccessLogItemTypeEnum.RESPONSE_SIZE,
            AccessLogItemTypeEnum.RESPONSE_SIZE_CLF,
            AccessLogItemTypeEnum.FIRST_LINE_OF_REQUEST,
            AccessLogItemTypeEnum.URL_PATH,
            AccessLogItemTypeEnum.QUERY_STRING,
            AccessLogItemTypeEnum.URL_PATH,
            AccessLogItemTypeEnum.QUERY_STRING,
            AccessLogItemTypeEnum.URL_PATH_WITH_QUERY,
            AccessLogItemTypeEnum.REQUEST_PROTOCOL,
            AccessLogItemTypeEnum.DATETIME_DEFAULT,
            AccessLogItemTypeEnum.DATETIME_CONFIGURABLE,
            AccessLogItemTypeEnum.DATETIME_CONFIGURABLE,
            AccessLogItemTypeEnum.REQUEST_HEADER,
            AccessLogItemTypeEnum.RESPONSE_HEADER,
            AccessLogItemTypeEnum.COOKIE,
            AccessLogItemTypeEnum.SCB_TRACE_ID));
  }

  @Test
  public void testCheckLocationList() {
    List<AccessLogItemLocation> locationList = new ArrayList<>(3);
    locationList.add(new AccessLogItemLocation().setStart(0).setEnd(3));
    locationList.add(new AccessLogItemLocation().setStart(3).setEnd(6));
    locationList.add(new AccessLogItemLocation().setStart(5).setEnd(9));

    try {
      Deencapsulation.invoke(new DefaultAccessLogPatternParser(), "checkLocationList",
          "0123456789", locationList);
      fail("expect an exception");
    } catch (Exception e) {
      assertEquals(IllegalArgumentException.class, e.getClass());
      assertEquals("access log pattern contains illegal placeholder, please check it.", e.getMessage());
    }
  }

  @Test
  public void testCheckLocationListOnLocationEndGreaterThanPatternLength() {
    List<AccessLogItemLocation> locationList = new ArrayList<>(3);
    locationList.add(new AccessLogItemLocation().setStart(0).setEnd(3));
    locationList.add(new AccessLogItemLocation().setStart(3).setEnd(6));
    locationList.add(new AccessLogItemLocation().setStart(7).setEnd(9));

    try {
      Deencapsulation.invoke(new DefaultAccessLogPatternParser(), "checkLocationList",
          "0123456", locationList);
      fail("expect an exception");
    } catch (Exception e) {
      assertEquals(IllegalArgumentException.class, e.getClass());
      assertEquals("access log pattern contains illegal placeholder, please check it.", e.getMessage());
    }
  }
}
