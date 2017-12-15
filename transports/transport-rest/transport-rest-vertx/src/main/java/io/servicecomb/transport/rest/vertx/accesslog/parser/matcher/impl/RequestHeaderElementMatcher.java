package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.RequestHeaderElement;

public class RequestHeaderElementMatcher extends ConfigurableAccessLogElementMatcher {
  public static final String PLACEHOLDER_PREFIX = "%{";

  public static final String PLACEHOLDER_SUFFIX = "}i";

  @Override
  protected String getPlaceholderSuffix() {
    return PLACEHOLDER_SUFFIX;
  }

  @Override
  protected String getPlaceholderPrefix() {
    return PLACEHOLDER_PREFIX;
  }

  @Override
  protected AccessLogElement getAccessLogElement(String identifier) {
    return new RequestHeaderElement(identifier);
  }
}
