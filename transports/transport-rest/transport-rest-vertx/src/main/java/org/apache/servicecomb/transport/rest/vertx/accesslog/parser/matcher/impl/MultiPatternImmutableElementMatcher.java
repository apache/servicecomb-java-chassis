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

import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;

public abstract class MultiPatternImmutableElementMatcher extends ImmutableAccessLogElementMatcher {
  @Override
  public List<AccessLogElementExtraction> extractElementPlaceholder(String rawPattern) {
    List<AccessLogElementExtraction> extractionList = new ArrayList<>();

    String[] patterns = getPlaceholderPatterns();
    for (String pattern : patterns) {
      matchElementPlaceholder(rawPattern, pattern, extractionList);
    }

    return extractionList;
  }

  protected abstract String[] getPlaceholderPatterns();
}
