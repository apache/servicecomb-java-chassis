package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.LocalPortElement;

public class LocalPortMatcher extends SinglePatternImmutableElementMatcher {

  public static final LocalPortElement ELEMENT = new LocalPortElement();

  @Override
  protected String getPlaceholderPattern() {
    return "%p";
  }

  @Override
  protected AccessLogElement getAccessLogElement() {
    return ELEMENT;
  }
}
