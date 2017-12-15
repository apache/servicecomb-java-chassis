package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;

public class DurationSecondElement implements AccessLogElement {
  @Override
  public String getFormattedElement(AccessLogParam accessLogParam) {
    return String.valueOf((accessLogParam.getEndMillisecond() - accessLogParam.getStartMillisecond()) / 1000);
  }
}
