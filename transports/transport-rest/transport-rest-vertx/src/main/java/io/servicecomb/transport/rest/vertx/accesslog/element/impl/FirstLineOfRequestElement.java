package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;

public class FirstLineOfRequestElement implements AccessLogElement {
  private static final MethodElement METHOD_ELEMENT = new MethodElement();

  private static final UriPathOnlyElement URI_PATH_ONLY_ELEMENT = new UriPathOnlyElement();

  private static final VersionOrProtocolElement VERSION_OR_PROTOCOL_ELEMENT = new VersionOrProtocolElement();

  @Override
  public String getFormattedElement(AccessLogParam accessLogParam) {
    StringBuilder result = new StringBuilder(64)
        .append("\"")
        .append(METHOD_ELEMENT.getFormattedElement(accessLogParam))
        .append(" ")
        .append(URI_PATH_ONLY_ELEMENT.getFormattedElement(accessLogParam))
        .append(" ")
        .append(VERSION_OR_PROTOCOL_ELEMENT.getFormattedElement(accessLogParam))
        .append("\"");

    return result.toString();
  }
}
