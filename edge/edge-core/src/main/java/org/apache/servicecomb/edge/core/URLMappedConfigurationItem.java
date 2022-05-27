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

package org.apache.servicecomb.edge.core;

import java.util.regex.Pattern;

public class URLMappedConfigurationItem {
  private String microserviceName;

  private String versionRule;

  private int prefixSegmentCount;

  private Pattern pattern;

  private String stringPattern;

  public String getMicroserviceName() {
    return microserviceName;
  }

  public void setMicroserviceName(String microserviceName) {
    this.microserviceName = microserviceName;
  }

  public String getVersionRule() {
    return versionRule;
  }

  public void setVersionRule(String versionRule) {
    this.versionRule = versionRule;
  }

  public int getPrefixSegmentCount() {
    return prefixSegmentCount;
  }

  public void setPrefixSegmentCount(int prefixSegmentCount) {
    this.prefixSegmentCount = prefixSegmentCount;
  }

  public Pattern getPattern() {
    return pattern;
  }

  public void setPattern(Pattern pattern) {
    this.pattern = pattern;
  }

  public String getStringPattern() {
    return stringPattern;
  }

  public void setStringPattern(String stringPattern) {
    this.stringPattern = stringPattern;
  }
}
