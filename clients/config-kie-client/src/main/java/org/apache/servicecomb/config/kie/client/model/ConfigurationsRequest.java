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

package org.apache.servicecomb.config.kie.client.model;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationsRequest implements Comparable<ConfigurationsRequest> {
  public static final String INITIAL_REVISION = "-1";

  private int order;

  private String revision = INITIAL_REVISION;

  private boolean withExact;

  private String labelsQuery;

  private Map<String, Object> lastRawData = new HashMap<>();

  public int getOrder() {
    return order;
  }

  public ConfigurationsRequest setOrder(int order) {
    this.order = order;
    return this;
  }

  public String getRevision() {
    return revision;
  }

  public ConfigurationsRequest setRevision(String revision) {
    this.revision = revision;
    return this;
  }

  public boolean isWithExact() {
    return withExact;
  }

  public ConfigurationsRequest setWithExact(boolean withExact) {
    this.withExact = withExact;
    return this;
  }

  public String getLabelsQuery() {
    return labelsQuery;
  }

  public ConfigurationsRequest setLabelsQuery(String labelsQuery) {
    this.labelsQuery = labelsQuery;
    return this;
  }

  public Map<String, Object> getLastRawData() {
    return lastRawData;
  }

  public ConfigurationsRequest setLastRawData(Map<String, Object> lastRawData) {
    this.lastRawData = lastRawData;
    return this;
  }

  @Override
  public int compareTo(ConfigurationsRequest o) {
    return o.getOrder() - this.order;
  }
}
