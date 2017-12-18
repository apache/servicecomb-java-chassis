/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import java.util.Set;

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
