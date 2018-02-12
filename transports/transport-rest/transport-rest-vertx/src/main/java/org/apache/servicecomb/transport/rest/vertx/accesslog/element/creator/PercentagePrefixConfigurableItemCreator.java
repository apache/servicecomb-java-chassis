package org.apache.servicecomb.transport.rest.vertx.accesslog.element.creator;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.CookieItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.PlainTextItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RequestHeaderItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.ResponseHeaderItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;

public class PercentagePrefixConfigurableItemCreator implements AccessLogItemCreator {
  @Override
  public AccessLogItem create(String rawPattern, AccessLogItemLocation location) {
    String config = getConfig(rawPattern, location);
    switch (location.getPlaceHolder()) {
      case DATETIME_CONFIGURABLE:
        return new DatetimeConfigurableItem(config);
      case REQUEST_HEADER:
        return new RequestHeaderItem(config);
      case RESPONSE_HEADER:
        return new ResponseHeaderItem(config);
      case COOKIE:
        return new CookieItem(config);
      case TEXT_PLAIN:
        return new PlainTextItem(config);
      default:
        // unexpected situation
        return null;
    }
  }

  private String getConfig(String rawPattern, AccessLogItemLocation location) {
    if (location.getPlaceHolder() == AccessLogItemTypeEnum.TEXT_PLAIN) {
      return rawPattern.substring(location.getStart(), location.getEnd());
    }
    return rawPattern.substring(location.getStart() + 2, location.getEnd() - 2);
  }
}
