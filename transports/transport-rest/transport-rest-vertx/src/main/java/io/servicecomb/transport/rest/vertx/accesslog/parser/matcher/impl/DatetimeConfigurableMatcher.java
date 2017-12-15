package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableElement;

/**
 * There are two kinds of configurable datetime placeholder:
 * <ul>
 *   <li>%{PATTERN}t</li>
 *   <li>%{PATTERN|TIMEZONE|LOCALE}t</li>
 * </ul>
 */
public class DatetimeConfigurableMatcher extends ConfigurableAccessLogElementMatcher {

  public static final String PLACEHOLDER_PREFIX = "%{";

  public static final String PLACEHOLDER_SUFFIX = "}t";


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
    return new DatetimeConfigurableElement(identifier);
  }
}
