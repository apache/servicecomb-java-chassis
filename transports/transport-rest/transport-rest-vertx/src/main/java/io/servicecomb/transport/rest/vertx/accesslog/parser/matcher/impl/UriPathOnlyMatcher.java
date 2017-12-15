package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.UriPathOnlyElement;

public class UriPathOnlyMatcher extends MultiPatternImmutableElementMatcher {

  public static final String[] PLACEHOLDER_PATTERNS = {"%U", "cs-uri-stem"};

  @Override
  protected String[] getPlaceholderPatterns() {
    return PLACEHOLDER_PATTERNS;
  }

  @Override
  protected AccessLogElement getAccessLogElement() {
    return new UriPathOnlyElement();
  }
}
