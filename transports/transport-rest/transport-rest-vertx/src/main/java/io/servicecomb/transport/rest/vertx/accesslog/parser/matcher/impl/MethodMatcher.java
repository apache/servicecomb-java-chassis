package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.MethodElement;

public class MethodMatcher extends MultiPatternImmutableElementMatcher {
  public static final String[] PLACEHOLDER_PATTERNS = {"%m", "cs-method"};

  public static final MethodElement ELEMENT = new MethodElement();

  @Override
  protected String[] getPlaceholderPatterns() {
    return PLACEHOLDER_PATTERNS;
  }

  @Override
  protected AccessLogElement getAccessLogElement() {
    return ELEMENT;
  }
}
