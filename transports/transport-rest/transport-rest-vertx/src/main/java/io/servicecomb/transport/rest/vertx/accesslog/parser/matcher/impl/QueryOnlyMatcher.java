package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.QueryOnlyElement;

public class QueryOnlyMatcher extends MultiPatternImmutableElementMatcher {

  public static final String[] PLACEHOLDER_PATTERNS = {"%q", "cs-uri-query"};

  @Override
  protected String[] getPlaceholderPatterns() {
    return PLACEHOLDER_PATTERNS;
  }

  @Override
  protected AccessLogElement getAccessLogElement() {
    return new QueryOnlyElement();
  }
}
