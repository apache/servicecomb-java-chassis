package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class BytesWrittenV1Element implements AccessLogElement {

  public static final String ZERO_BYTES = "0";

  @Override
  public String getFormattedElement(AccessLogParam accessLogParam) {
    HttpServerResponse response = accessLogParam.getRoutingContext().response();
    if (null == response) {
      return ZERO_BYTES;
    }

    long bytesWritten = response.bytesWritten();

    return String.valueOf(bytesWritten);
  }
}
