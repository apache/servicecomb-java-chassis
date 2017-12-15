package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;

/**
 * Print content as it is.
 */
public class PlainTextElement implements AccessLogElement {
  private final String content;

  public PlainTextElement(String content) {
    this.content = content;
  }

  @Override
  public String getFormattedElement(AccessLogParam accessLogParam) {
    return content;
  }
}
