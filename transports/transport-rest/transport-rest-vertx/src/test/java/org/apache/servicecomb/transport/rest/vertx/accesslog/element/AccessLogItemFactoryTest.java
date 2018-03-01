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

package org.apache.servicecomb.transport.rest.vertx.accesslog.element;

import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.PlainTextItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.QueryStringItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RequestHeaderItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.UrlPathWithQueryItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;
import org.junit.Assert;
import org.junit.Test;

import io.vertx.ext.web.RoutingContext;

public class AccessLogItemFactoryTest {
  private static final String PATTERN = "test %{EEE, dd MMM yyyy HH:mm:ss zzz}t cs-uri-query cs-uri %{VARNAME1}i";

  private static final List<AccessLogItemLocation> locationList = Arrays.asList(
      new AccessLogItemLocation().setStart(0).setEnd(5).setPlaceHolder(AccessLogItemTypeEnum.TEXT_PLAIN),
      new AccessLogItemLocation().setStart(5).setEnd(38).setPlaceHolder(AccessLogItemTypeEnum.DATETIME_CONFIGURABLE),
      new AccessLogItemLocation().setStart(39).setEnd(51).setPlaceHolder(AccessLogItemTypeEnum.QUERY_STRING),
      new AccessLogItemLocation().setStart(52).setEnd(58).setPlaceHolder(AccessLogItemTypeEnum.URL_PATH_WITH_QUERY),
      new AccessLogItemLocation().setStart(59).setEnd(71).setPlaceHolder(AccessLogItemTypeEnum.REQUEST_HEADER));

  @Test
  public void testCreateAccessLogItem() {
    List<AccessLogItem<RoutingContext>> itemList =
        new AccessLogItemFactory().createAccessLogItem(PATTERN, locationList);
    Assert.assertEquals(5, itemList.size());
    Assert.assertEquals(PlainTextItem.class, itemList.get(0).getClass());
    Assert.assertEquals(DatetimeConfigurableItem.class, itemList.get(1).getClass());
    Assert.assertEquals(QueryStringItem.class, itemList.get(2).getClass());
    Assert.assertEquals(UrlPathWithQueryItem.class, itemList.get(3).getClass());
    Assert.assertEquals(RequestHeaderItem.class, itemList.get(4).getClass());
  }
}
