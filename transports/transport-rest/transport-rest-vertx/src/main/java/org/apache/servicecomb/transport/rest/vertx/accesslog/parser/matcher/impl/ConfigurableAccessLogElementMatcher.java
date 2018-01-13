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

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.AccessLogElementMatcher;

public abstract class ConfigurableAccessLogElementMatcher implements AccessLogElementMatcher {

  @Override
  public List<AccessLogElementExtraction> extractElementPlaceholder(String rawPattern) {
    List<AccessLogElementExtraction> extractionList = new ArrayList<>();
    int begin = -1;
    int end = 0;
    int cursor = 0;

    while (true) {
      end = rawPattern.indexOf(getPlaceholderSuffix(), cursor);
      if (end < 0) {
        break;
      }
      begin = locateBeginIndex(rawPattern, end, cursor);
      if (begin < 0) {
        break;
      }

      String identifier = rawPattern.substring(begin + getPlaceholderSuffix().length(), end);
      extractionList.add(new AccessLogElementExtraction(begin, end + getPlaceholderSuffix().length(),
          getAccessLogElement(identifier)));

      cursor = end + 1;
    }
    return extractionList;
  }

  private int locateBeginIndex(String rawPattern, int end, int cursor) {
    int preBegin = rawPattern.indexOf(getPlaceholderPrefix(), cursor);
    int begin = rawPattern.indexOf(getPlaceholderPrefix(), preBegin + 1);
    while (begin >= 0 && begin < end) {
      if (begin < end) {
        preBegin = begin;
      }
      begin = rawPattern.indexOf(getPlaceholderPrefix(), preBegin + 1);
    }
    return preBegin;
  }


  protected abstract String getPlaceholderSuffix();

  protected abstract String getPlaceholderPrefix();

  protected abstract AccessLogElement getAccessLogElement(String identifier);
}
