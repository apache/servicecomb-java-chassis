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
package org.apache.servicecomb.foundation.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;

public class TestJsonUtils {
  @Test
  public void testJSR310() throws Exception {
    String period = "\"P1Y1M2D\"";
    String duration = "\"P1DT1H2M3S\"";
    String offsetDateTime = "\"2018-05-28T11:00:00+08:00\"";
    String localDate = "\"2018-05-28\"";
    String localDateTime = "\"2018-05-28T11:00:00\"";

    Assert.assertEquals(
        Period.of(1, 1, 2),
        JsonUtils.OBJ_MAPPER.readValue(period, Period.class)
    );
    Assert.assertEquals(
        Duration.ofDays(1).plusHours(1).plusMinutes(2).plusSeconds(3),
        JsonUtils.OBJ_MAPPER.readValue(duration, Duration.class)
    );
    Assert.assertEquals(
        OffsetDateTime.of(2018, 5, 28, 3, 0, 0, 0, ZoneOffset.ofHours(0)),
        JsonUtils.OBJ_MAPPER.readValue(offsetDateTime, OffsetDateTime.class)
    );
    Assert.assertEquals(
        LocalDate.of(2018, 5, 28),
        JsonUtils.OBJ_MAPPER.readValue(localDate, LocalDate.class)
    );
    Assert.assertEquals(
        LocalDateTime.of(2018, 5, 28, 11, 0, 0),
        JsonUtils.OBJ_MAPPER.readValue(localDateTime, LocalDateTime.class)
    );
  }
}
