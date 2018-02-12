package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher;

import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;

public interface AccessLogItemMatcher {
  /**
   * Return an {@link AccessLogItemLocation} which matches part of rawPattern and is nearest to the offset(That means
   * the {@link AccessLogItemLocation#start} is no less than offset and is smallest among the potential matched Item).
   * @param rawPattern
   * @param offset
   * @return
   */
  AccessLogItemLocation match(String rawPattern, int offset);
}
