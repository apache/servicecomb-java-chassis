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

import java.util.List;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.AccessLogElementMatcher;

/**
 * There are log elements that have no configuration parameter, so these elements can be immutable (and singleton).
 * <br/>
 * Therefore, the matching algorithm implementation of these elements can be extracted into a universal method.
 */
public abstract class ImmutableAccessLogElementMatcher implements AccessLogElementMatcher {

  protected void matchElementPlaceholder(String rawPattern, String pattern,
      List<AccessLogElementExtraction> extractionList) {
    int start = -1;
    int cursor = 0;
    while (true) {
      start = rawPattern.indexOf(pattern, cursor);

      if (start < 0) {
        break;
      } else {
        AccessLogElementExtraction extraction = new AccessLogElementExtraction(start, start + pattern.length(),
            getAccessLogElement());
        extractionList.add(extraction);
      }

      cursor = start + pattern.length();
    }
  }

  protected abstract AccessLogElement getAccessLogElement();
}
