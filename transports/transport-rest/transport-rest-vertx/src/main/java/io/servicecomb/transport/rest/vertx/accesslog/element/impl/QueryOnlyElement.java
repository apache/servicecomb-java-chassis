package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import org.springframework.util.StringUtils;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.vertx.core.http.HttpServerRequest;

public class QueryOnlyElement implements AccessLogElement {

  public static final String EMPTY_RESULT = "-";

  @Override
  public String getFormattedElement(AccessLogParam accessLogParam) {
    HttpServerRequest request = accessLogParam.getRoutingContext().request();
    if (null == request) {
      return EMPTY_RESULT;
    }

    String query = request.query();
    if (StringUtils.isEmpty(query)) {
      return EMPTY_RESULT;
    }
    return query;
  }
}
