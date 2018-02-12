package org.apache.servicecomb.transport.rest.vertx.accesslog.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.creator.AccessLogItemCreator;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.creator.PercentagePrefixConfigurableItemCreator;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.creator.SimpleAccessLogItemCreator;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;

public class AccessLogItemFactory {
  private List<AccessLogItemCreator> creatorList = Arrays
      .asList(new SimpleAccessLogItemCreator(), new PercentagePrefixConfigurableItemCreator());

  public List<AccessLogElement> createAccessLogItem(String rawPattern, List<AccessLogItemLocation> locationList) {
    List<AccessLogElement> itemList = new ArrayList<>();
    for (AccessLogItemLocation location : locationList) {
      setItemList(rawPattern, itemList, location);
    }

    return itemList;
  }

  private void setItemList(String rawPattern, List<AccessLogElement> itemList, AccessLogItemLocation location) {
    AccessLogElement item = null;
    for (AccessLogItemCreator creator : creatorList) {
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
