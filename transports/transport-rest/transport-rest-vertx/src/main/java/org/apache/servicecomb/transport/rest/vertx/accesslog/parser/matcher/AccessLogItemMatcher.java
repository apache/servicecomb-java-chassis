package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher;

import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;

public interface AccessLogItemMatcher {
  AccessLogItemLocation match(String rawPattern, int offset);
}
