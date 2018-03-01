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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.creator.AccessLogItemCreator;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.creator.PercentagePrefixConfigurableItemCreator;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.creator.SimpleAccessLogItemCreator;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;

import io.vertx.ext.web.RoutingContext;

/**
 * The factory of {@link AccessLogItem}.
 * Using the {@link AccessLogItemCreator} to generate AccessLogItem, according to {@link AccessLogItemLocation}
 * and rawPattern.
 */
public class AccessLogItemFactory {
  private List<AccessLogItemCreator<RoutingContext>> creatorList = Arrays
      .asList(new SimpleAccessLogItemCreator(), new PercentagePrefixConfigurableItemCreator());

  public List<AccessLogItem<RoutingContext>> createAccessLogItem(String rawPattern,
      List<AccessLogItemLocation> locationList) {
    List<AccessLogItem<RoutingContext>> itemList = new ArrayList<>();
    for (AccessLogItemLocation location : locationList) {
      setItemList(rawPattern, itemList, location);
    }

    return itemList;
  }

  /**
   * generate single AccessLogItem
   */
  private void setItemList(String rawPattern, List<AccessLogItem<RoutingContext>> itemList,
      AccessLogItemLocation location) {
    AccessLogItem<RoutingContext> item = null;
    for (AccessLogItemCreator<RoutingContext> creator : creatorList) {
      item = creator.create(rawPattern, location);
      if (null != item) {
        break;
      }
    }

    if (null != item) {
      itemList.add(item);
    }
  }
}
