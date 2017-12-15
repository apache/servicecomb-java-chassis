package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import org.springframework.util.StringUtils;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.vertx.core.http.HttpServerRequest;

public class UriPathIncludeQueryElement implements AccessLogElement {

  public static final String EMPTY_RESULT = "-";

  @Override
  public String getFormattedElement(AccessLogParam accessLogParam) {
    HttpServerRequest request = accessLogParam.getRoutingContext().request();
    if (null == request) {
      return EMPTY_RESULT;
    }

    String uri = request.uri();
    if (StringUtils.isEmpty(uri)) {
      return EMPTY_RESULT;
    }

    return uri;
  }
}
