/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl;

import java.util.Set;

import org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;

import io.vertx.ext.web.Cookie;

public class CookieElement implements AccessLogElement {

  public static final String RESULT_NOT_FOUND = "-";

  private final String identifier;

  public CookieElement(String identifier) {
    this.identifier = identifier;
  }

  @Override
  public String getFormattedElement(AccessLogParam accessLogParam) {
    Set<Cookie> cookieSet = accessLogParam.getRoutingContext().cookies();
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
