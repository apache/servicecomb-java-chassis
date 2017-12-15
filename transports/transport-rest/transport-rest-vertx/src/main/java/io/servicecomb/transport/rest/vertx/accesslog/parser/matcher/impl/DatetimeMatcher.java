package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableElement;

public class DatetimeMatcher extends SinglePatternImmutableElementMatcher {

  public static final DatetimeConfigurableElement ELEMENT = new DatetimeConfigurableElement();

  @Override
  protected String getPlaceholderPattern() {
    return "%t";
  }

  @Override
  protected AccessLogElement getAccessLogElement() {
    return ELEMENT;
  }
}
