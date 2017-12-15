package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import java.util.Set;

import com.sun.org.apache.regexp.internal.RE;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;

public class CookieElement implements AccessLogElement {

  public static final String RESULT_NOT_FOUND = "-";

  private final String identifier;

  public CookieElement(String identifier) {
    this.identifier = identifier;
  }

  @Override
  public String getFormattedElement(AccessLogParam accessLogParam) {
    RoutingContext context = accessLogParam.getRoutingContext();
    if (null == context) {
      return RESULT_NOT_FOUND;
    }

    if (context.cookieCount() == 0) {
      return RESULT_NOT_FOUND;
    }

    Set<Cookie> cookieSet = context.cookies();
    if (null == cookieSet) {
      return RESULT_NOT_FOUND;
    }

    String result = null;
    for (Cookie cookie : cookieSet) {
      if (identifier.equals(cookie.getName())) {
        result = cookie.getValue();
      }
    }

    if (null == result) {
      return RESULT_NOT_FOUND;
    }

    return result;
  }

  public String getIdentifier() {
    return identifier;
  }
}
