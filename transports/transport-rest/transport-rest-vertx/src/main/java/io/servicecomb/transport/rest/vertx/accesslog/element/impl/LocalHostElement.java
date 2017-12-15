package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import org.springframework.util.StringUtils;

import io.netty.channel.local.LocalAddress;
import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;

public class LocalHostElement implements AccessLogElement {

  public static final String EMPTY_RESULT = "-";

  @Override
  public String getFormattedElement(AccessLogParam accessLogParam) {
    HttpServerRequest request = accessLogParam.getRoutingContext().request();
    if (null == request) {
      return EMPTY_RESULT;
    }

    SocketAddress localAddress = request.localAddress();
    if (null == localAddress) {
      return EMPTY_RESULT;
    }

    String localHost = localAddress.host();
    if (StringUtils.isEmpty(localHost)) {
      return EMPTY_RESULT;
    }
    return localHost;
  }
}
