package org.apache.servicecomb.transport.rest.vertx.accesslog.element.creator;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;

public interface AccessLogItemCreator {
  AccessLogItem create(String rawPattern, AccessLogItemLocation location);
}
