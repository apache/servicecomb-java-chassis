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

import java.util.List;

import org.apache.servicecomb.governance.entity.Configurable;

public class TrafficMarker extends Configurable {
  private List<Matcher> matches;

  @Override
  public boolean isValid() {
    if (matches == null || matches.isEmpty()) {
      return false;
    }
    return true;
  }

  public List<Matcher> getMatches() {
    return matches;
  }

  public void setMatches(List<Matcher> matches) {
    this.matches = matches;
  }

  public boolean checkMatch(GovernanceRequest governanceRequest, RequestProcessor requestProcessor) {
    return this.matches.stream().anyMatch(match -> requestProcessor.match(governanceRequest, match));
  }
}
