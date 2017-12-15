package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.RemoteHostElement;

public class RemoteHostMatcher extends SinglePatternImmutableElementMatcher {

  public static final RemoteHostElement ELEMENT = new RemoteHostElement();

  @Override
  protected String getPlaceholderPattern() {
    return "%h";
  }

  @Override
  protected AccessLogElement getAccessLogElement() {
    return ELEMENT;
  }
}
