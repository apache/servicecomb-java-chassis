package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.VersionOrProtocolElement;

public class VersionOrProtocolMatcher extends SinglePatternImmutableElementMatcher {

  public static final VersionOrProtocolElement ELEMENT = new VersionOrProtocolElement();

  @Override
  protected String getPlaceholderPattern() {
    return "%H";
  }

  @Override
  protected AccessLogElement getAccessLogElement() {
    return ELEMENT;
  }
}
