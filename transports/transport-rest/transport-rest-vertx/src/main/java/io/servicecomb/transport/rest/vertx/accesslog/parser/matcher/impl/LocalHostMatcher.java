package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.LocalHostElement;

public class LocalHostMatcher extends SinglePatternImmutableElementMatcher {

  public static final LocalHostElement ELEMENT = new LocalHostElement();

  @Override
  protected String getPlaceholderPattern() {
    return "%v";
  }

  @Override
  protected AccessLogElement getAccessLogElement() {
    return ELEMENT;
  }
}
