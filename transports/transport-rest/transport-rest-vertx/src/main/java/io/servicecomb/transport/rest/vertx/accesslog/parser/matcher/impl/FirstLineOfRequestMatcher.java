package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.FirstLineOfRequestElement;

public class FirstLineOfRequestMatcher extends SinglePatternImmutableElementMatcher {

  public static final FirstLineOfRequestElement ELEMENT = new FirstLineOfRequestElement();

  @Override
  protected String getPlaceholderPattern() {
    return "%r";
  }

  @Override
  protected AccessLogElement getAccessLogElement() {
    return ELEMENT;
  }
}
