package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher;

import java.util.List;

import io.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;

/**
 * match placeholder in raw pattern
 */
public interface AccessLogElementMatcher {
  /**
   * extract placeholders from rawPattern that match this element.
   *
   * @param rawPattern
   * @return
   */
  List<AccessLogElementExtraction> extractElementPlaceholder(String rawPattern);
}
