package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerResponse;

public class ResponseHeaderElement implements AccessLogElement {

  public static final String RESULT_NOT_FOUND = "-";

  private final String identifier;

  public ResponseHeaderElement(String identifier) {
    this.identifier = identifier;
  }

  @Override
  public String getFormattedElement(AccessLogParam accessLogParam) {
    HttpServerResponse response = accessLogParam.getRoutingContext().response();
    if (null == response) {
      return RESULT_NOT_FOUND;
    }

    MultiMap headers = response.headers();
    if (null == headers) {
      return RESULT_NOT_FOUND;
    }

    String result = headers.get(identifier);
    if (null == result) {
      return RESULT_NOT_FOUND;
    }

    return result;
  }

  public String getIdentifier() {
    return identifier;
  }
}
