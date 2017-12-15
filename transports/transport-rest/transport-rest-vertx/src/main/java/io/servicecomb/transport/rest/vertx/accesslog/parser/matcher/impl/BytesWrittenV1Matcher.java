package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.BytesWrittenV1Element;

public class BytesWrittenV1Matcher extends SinglePatternImmutableElementMatcher {

  public static final BytesWrittenV1Element ELEMENT = new BytesWrittenV1Element();

  @Override
  protected String getPlaceholderPattern() {
    return "%B";
  }

  @Override
  protected AccessLogElement getAccessLogElement() {
    return ELEMENT;
  }
}
