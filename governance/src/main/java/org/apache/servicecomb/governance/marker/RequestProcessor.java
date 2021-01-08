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
package org.apache.servicecomb.governance.marker;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.governance.marker.operator.MatchOperator;
import org.apache.servicecomb.governance.marker.operator.RawOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RequestProcessor {

  private static final String OPERATOR_SUFFIX = "Operator";

  @Autowired
  private Map<String, MatchOperator> operatorMap;

  public boolean match(GovernanceRequest request, Matcher matcher) {
    if ((matcher.getMethod() != null && !matcher.getMethod().contains(request.getMethod())) ||
        (matcher.getApiPath() != null && !operatorMatch(request.getUri(), matcher.getApiPath()))) {
      return false;
    }
    if (matcher.getHeaders() == null) {
      return true;
    }
    for (Entry<String, RawOperator> entry : matcher.getHeaders().entrySet()) {
      if (!request.getHeaders().containsKey(entry.getKey()) ||
          !operatorMatch(request.getHeaders().get(entry.getKey()), entry.getValue())) {
        return false;
      }
    }
    return true;
  }

  private boolean operatorMatch(String str, RawOperator rawOperator) {
    if (rawOperator.isEmpty()) {
      return false;
    }

    for (Entry<String, String> entry : rawOperator.entrySet()) {
      if (!operatorMap.get(entry.getKey() + OPERATOR_SUFFIX).match(str, entry.getValue())) {
        return false;
      }
    }
    return true;
  }
}
