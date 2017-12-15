package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.StatusElement;

public class StatusMatcher extends MultiPatternImmutableElementMatcher {

  public static final String[] PLACEHOLDER_PATTERNS = {"%s", "cs-status"};

  @Override
  protected String[] getPlaceholderPatterns() {
    return PLACEHOLDER_PATTERNS;
  }

  @Override
  protected AccessLogElement getAccessLogElement() {
    return new StatusElement();
  }
}
