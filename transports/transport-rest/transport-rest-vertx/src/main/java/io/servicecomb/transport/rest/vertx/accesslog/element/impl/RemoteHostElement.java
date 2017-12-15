package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import org.springframework.util.StringUtils;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;

public class RemoteHostElement implements AccessLogElement {

  public static final String EMPTY_RESULT = "-";

  @Override
  public String getFormattedElement(AccessLogParam accessLogParam) {
    HttpServerRequest request = accessLogParam.getRoutingContext().request();
    if (null == request) {
      return EMPTY_RESULT;
    }

    SocketAddress remoteAddress = request.remoteAddress();
    if (null == remoteAddress) {
      return EMPTY_RESULT;
    }

    String remoteHost = remoteAddress.host();
    if (StringUtils.isEmpty(remoteHost)) {
      return EMPTY_RESULT;
    }
    return remoteHost;
  }
}
