package io.servicecomb.transport.rest.vertx.accesslog.element;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;

/**
 * element should be printed into access log.
 */
public interface AccessLogElement {
  /**
   * find out specified content from {@link AccessLogParam}, format the content and return it.
   *
   * @param accessLogParam
   * @return
   */
  String getFormattedElement(AccessLogParam accessLogParam);
}
