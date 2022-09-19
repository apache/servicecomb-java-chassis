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
package org.apache.servicecomb.foundation.protobuf;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestISODateTimeParsing {
  @Test
  public void testParseStringToDate() {
    SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD'T'hh:mm:ssZ");
    sdf.setTimeZone(TimeZone.getTimeZone("EST"));

    OffsetDateTime offsetDateTime = OffsetDateTime.parse("2022-05-31T09:16:38.941Z");
    Date d = Date.from(offsetDateTime.toInstant());
    String date = sdf.format(d);
    Assertions.assertEquals("2022-05-151T04:16:38-0500", date);

    offsetDateTime = OffsetDateTime.parse("2022-05-31T09:16:38.941+00:00");
    d = Date.from(offsetDateTime.toInstant());
    date = sdf.format(d);
    Assertions.assertEquals("2022-05-151T04:16:38-0500", date);
  }
}
