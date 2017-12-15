package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.DurationMillisecondElement;

public class DurationMillisecondMatcher extends SinglePatternImmutableElementMatcher {

  public static final DurationMillisecondElement ELEMENT = new DurationMillisecondElement();

  @Override
  protected String getPlaceholderPattern() {
    return "%D";
  }

  @Override
  protected AccessLogElement getAccessLogElement() {
    return ELEMENT;
  }
}
