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

package org.apache.servicecomb.governance.policy;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class GovernanceRule {
  private static final String MATCH_NONE = "none";

  private String match;

  private List<String> parsedMatch;

  private int precedence;

  public String getMatch() {
    return match;
  }

  public void setMatch(String match) {
    this.match = match;
  }

  public int getPrecedence() {
    return precedence;
  }

  public void setPrecedence(int precedence) {
    this.precedence = precedence;
  }

  public boolean match(String name) {
    if (StringUtils.isEmpty(this.match)) {
      return false;
    }

    if (MATCH_NONE.equals(this.match)) {
      return true;
    }

    if (this.parsedMatch == null) {
      this.parsedMatch = Arrays.asList(this.match.split(","));
    }

    return parsedMatch.contains(name);
  }

  public List<String> getParsedMatch() {
    if (StringUtils.isEmpty(this.match)) {
      return null;
    }

    if (MATCH_NONE.equals(this.match)) {
      return null;
    }

    if (this.parsedMatch == null) {
      this.parsedMatch = Arrays.asList(this.match.split(","));
    }

    return parsedMatch;
  }
}
