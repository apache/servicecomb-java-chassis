package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.DurationSecondElement;

public class DurationSecondMatcher extends SinglePatternImmutableElementMatcher {

  public static final DurationSecondElement ELEMENT = new DurationSecondElement();

  @Override
  protected String getPlaceholderPattern() {
    return "%T";
  }

  @Override
  protected AccessLogElement getAccessLogElement() {
    return ELEMENT;
  }
}
