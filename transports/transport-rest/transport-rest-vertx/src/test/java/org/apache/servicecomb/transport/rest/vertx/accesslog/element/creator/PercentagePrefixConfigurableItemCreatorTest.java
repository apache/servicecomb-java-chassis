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

package org.apache.servicecomb.transport.rest.vertx.accesslog.element.creator;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.CookieItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.PlainTextItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RequestHeaderItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.ResponseHeaderItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;
import org.junit.Assert;
import org.junit.Test;

import io.vertx.ext.web.RoutingContext;

public class PercentagePrefixConfigurableItemCreatorTest {
  private static final String PATTERN = "test %{EEE, dd MMM yyyy HH:mm:ss zzz}t %{VARNAME1}i %{VARNAME2}o %{VARNAME3}C";

  private static final PercentagePrefixConfigurableItemCreator CREATOR = new PercentagePrefixConfigurableItemCreator();

  @Test
  public void testCreateDatetimeConfigurableItem() {
    AccessLogItemLocation location = new AccessLogItemLocation().setStart(5).setEnd(38).setPlaceHolder(
        AccessLogItemTypeEnum.DATETIME_CONFIGURABLE);

    AccessLogItem<RoutingContext> item = CREATOR.create(PATTERN, location);

    Assert.assertEquals(DatetimeConfigurableItem.class, item.getClass());
    Assert.assertEquals("EEE, dd MMM yyyy HH:mm:ss zzz", ((DatetimeConfigurableItem) item).getPattern());
  }

  @Test
  public void testCreateRequestHeaderItem() {
    AccessLogItemLocation location = new AccessLogItemLocation().setStart(39).setEnd(51).setPlaceHolder(
        AccessLogItemTypeEnum.REQUEST_HEADER);

    AccessLogItem<RoutingContext> item = CREATOR.create(PATTERN, location);

    Assert.assertEquals(RequestHeaderItem.class, item.getClass());
    Assert.assertEquals("VARNAME1", ((RequestHeaderItem) item).getVarName());
  }


  @Test
  public void testCreateResponseHeaderItem() {
    AccessLogItemLocation location = new AccessLogItemLocation().setStart(52).setEnd(64).setPlaceHolder(
        AccessLogItemTypeEnum.RESPONSE_HEADER);

    AccessLogItem<RoutingContext> item = CREATOR.create(PATTERN, location);

    Assert.assertEquals(ResponseHeaderItem.class, item.getClass());
    Assert.assertEquals("VARNAME2", ((ResponseHeaderItem) item).getVarName());
  }

  @Test
  public void testCreateCookieItem() {
    AccessLogItemLocation location = new AccessLogItemLocation().setStart(65).setEnd(77).setPlaceHolder(
        AccessLogItemTypeEnum.COOKIE);

    AccessLogItem<RoutingContext> item = CREATOR.create(PATTERN, location);

    Assert.assertEquals(CookieItem.class, item.getClass());
    Assert.assertEquals("VARNAME3", ((CookieItem) item).getVarName());
  }

  @Test
  public void testPlainTextItem() {
    AccessLogItemLocation location = new AccessLogItemLocation().setStart(0)
        .setEnd(5)
        .setPlaceHolder(AccessLogItemTypeEnum.TEXT_PLAIN);

    AccessLogItem<RoutingContext> item = CREATOR.create(PATTERN, location);

    Assert.assertEquals(PlainTextItem.class, item.getClass());
    Assert.assertEquals("test ", ((PlainTextItem) item).getFormattedItem(null));
  }
}
