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

package org.apache.servicecomb.demo.springmvc.client;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.springframework.stereotype.Component;

interface DateTimeSchemaInf {
  Date getDate(Date date);

  Date getDatePath(Date date);

  Date postDate(Date date);

  LocalDate getLocalDate(LocalDate date);

  LocalDate getLocalDatePath(LocalDate date);

  LocalDate postLocalDate(LocalDate date);

  LocalDateTime getLocalDateTime(LocalDateTime date);

  LocalDateTime getLocalDateTimePath(LocalDateTime date);

  LocalDateTime postLocalDateTime(LocalDateTime date);
}

@Component
public class TestDateTimeSchema implements CategorizedTestCase {
  @RpcReference(microserviceName = "springmvc", schemaId = "DateTimeSchema")
  private DateTimeSchemaInf dateTimeSchemaInf;

  @Override
  public void testRestTransport() throws Exception {

  }

  @Override
  public void testHighwayTransport() throws Exception {

  }

  @Override
  public void testAllTransport() throws Exception {
    testDateTimeSchema();
  }

  private void testDateTimeSchema() {
    Date date = new Date();
    TestMgr.check(date.getTime(), dateTimeSchemaInf.getDate(date).getTime());
    TestMgr.check(date.getTime(), dateTimeSchemaInf.getDatePath(date).getTime());
    TestMgr.check(date.getTime(), dateTimeSchemaInf.postDate(date).getTime());

    LocalDate localDate = LocalDate.of(2020, 2, 1);
    TestMgr.check(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        dateTimeSchemaInf.getLocalDate(localDate).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    TestMgr.check(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        dateTimeSchemaInf.getLocalDatePath(localDate).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    TestMgr.check(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        dateTimeSchemaInf.postLocalDate(localDate).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

    LocalDateTime localDateTime = LocalDateTime.of(2020, 2, 1, 23, 23, 30, 333);
    TestMgr.check(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")),
        dateTimeSchemaInf.getLocalDateTime(localDateTime)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")));
    TestMgr.check(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")),
        dateTimeSchemaInf.getLocalDateTimePath(localDateTime)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")));
    TestMgr.check(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")),
        dateTimeSchemaInf.postLocalDateTime(localDateTime)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")));
  }
}
