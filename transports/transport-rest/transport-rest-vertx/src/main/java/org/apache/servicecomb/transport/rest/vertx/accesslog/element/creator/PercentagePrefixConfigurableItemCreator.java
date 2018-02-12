package org.apache.servicecomb.transport.rest.vertx.accesslog.element.creator;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.CookieElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.PlainTextElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RequestHeaderElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.ResponseHeaderElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;

public class PercentagePrefixConfigurableItemCreator implements AccessLogItemCreator {
  @Override
  public AccessLogElement create(String rawPattern, AccessLogItemLocation location) {
    String config = getConfig(rawPattern, location);
    switch (location.getPlaceHolder()) {
      case DATETIME_CONFIGURABLE:
        return new DatetimeConfigurableElement(config);
      case REQUEST_HEADER:
        return new RequestHeaderElement(config);
      case RESPONSE_HEADER:
        return new ResponseHeaderElement(config);
      case COOKIE:
        return new CookieElement(config);
      case TEXT_PLAIN:
        return new PlainTextElement(config);
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
