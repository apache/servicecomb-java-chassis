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

package org.apache.servicecomb.config.center.client.model;

import java.util.Map;

public class QueryConfigurationsResponse {
  private String revision;

  private boolean changed;

  private Map<String, Object> configurations;

  public String getRevision() {
    return revision;
  }

  public QueryConfigurationsResponse setRevision(String revision) {
    this.revision = revision;
    return this;
  }

  public boolean isChanged() {
    return changed;
  }

  public QueryConfigurationsResponse setChanged(boolean changed) {
    this.changed = changed;
    return this;
  }

  public Map<String, Object> getConfigurations() {
    return configurations;
  }

  public QueryConfigurationsResponse setConfigurations(
      Map<String, Object> configurations) {
    this.configurations = configurations;
    return this;
  }
}
