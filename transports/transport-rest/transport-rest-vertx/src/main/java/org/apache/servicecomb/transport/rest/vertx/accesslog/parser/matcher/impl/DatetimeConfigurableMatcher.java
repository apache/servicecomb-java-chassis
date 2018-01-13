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

package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableElement;

/**
 * Compatible with two kinds of configurable datetime placeholder:
 * <ul>
 *   <li>v1: %{PATTERN}t</li>
 *   <li>v2: %{PATTERN|TIMEZONE|LOCALE}t</li>
 * </ul>
 */
public class DatetimeConfigurableMatcher extends ConfigurableAccessLogElementMatcher {

  public static final String PLACEHOLDER_PREFIX = "%{";

  public static final String PLACEHOLDER_SUFFIX = "}t";


  @Override
  protected String getPlaceholderSuffix() {
    return PLACEHOLDER_SUFFIX;
  }

  @Override
  protected String getPlaceholderPrefix() {
    return PLACEHOLDER_PREFIX;
  }

  @Override
  protected AccessLogElement getAccessLogElement(String identifier) {
    return new DatetimeConfigurableElement(identifier);
  }
}
