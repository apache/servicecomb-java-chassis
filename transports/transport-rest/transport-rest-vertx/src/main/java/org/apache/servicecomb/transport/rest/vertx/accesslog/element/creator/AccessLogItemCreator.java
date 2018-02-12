package org.apache.servicecomb.transport.rest.vertx.accesslog.element.creator;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;

public interface AccessLogItemCreator {
  AccessLogElement create(String rawPattern, AccessLogItemLocation location);
}
