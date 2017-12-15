package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.BytesWrittenV2Element;

public class BytesWrittenV2Matcher extends SinglePatternImmutableElementMatcher {

  public static final BytesWrittenV2Element ELEMENT = new BytesWrittenV2Element();

  @Override
  protected String getPlaceholderPattern() {
    return "%b";
  }

  @Override
  protected AccessLogElement getAccessLogElement() {
    return ELEMENT;
  }
}
