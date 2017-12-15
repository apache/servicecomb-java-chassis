package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.vertx.core.http.HttpServerResponse;

public class BytesWrittenV2Element implements AccessLogElement {

  public static final String ZERO_BYTES = "-";

  @Override
  public String getFormattedElement(AccessLogParam accessLogParam) {
    HttpServerResponse response = accessLogParam.getRoutingContext().response();
    if (null == response) {
      return ZERO_BYTES;
    }

    long bytesWritten = response.bytesWritten();
    if (0 == bytesWritten) {
      return ZERO_BYTES;
    } else {
      return String.valueOf(bytesWritten);
    }
  }
}
