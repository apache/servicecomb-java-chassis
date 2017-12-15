package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpVersion;

public class VersionOrProtocolElement implements AccessLogElement {

  public static final String EMPTY_RESULT = "-";

  @Override
  public String getFormattedElement(AccessLogParam accessLogParam) {
    HttpServerRequest request = accessLogParam.getRoutingContext().request();
    if (null == request) {
      return EMPTY_RESULT;
    }
    if (null == request.version()) {
      return EMPTY_RESULT;
    }
    return getStringVersion(request.version());
  }

  private String getStringVersion(HttpVersion version) {
    switch (version) {
      case HTTP_2:
        return "HTTP/2.0";
      case HTTP_1_0:
        return "HTTP/1.0";
      case HTTP_1_1:
        return "HTTP/1.1";
      default:
        return EMPTY_RESULT;
    }
  }
}
