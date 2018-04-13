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
package org.apache.servicecomb.samples.mwf;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class CloudEyeMetricModel {
  private String node;

  private String scope_name;

  private long timestamp;

  private String inface_name;

  @JsonAnySetter
  private Map<String, String> dynamicValue = new HashMap<>();

  public String getNode() {
    return node;
  }

  public void setNode(String node) {
    this.node = node;
  }

  public String getScope_name() {
    return scope_name;
  }

  public void setScope_name(String scope_name) {
    this.scope_name = scope_name;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getInface_name() {
    return inface_name;
  }

  public void setInface_name(String inface_name) {
    this.inface_name = inface_name;
  }

  @JsonAnyGetter
  public Map<String, String> getDynamicValue() {
    return dynamicValue;
  }

  public void setDynamicValue(Map<String, String> dynamicValue) {
    this.dynamicValue = dynamicValue;
  }
}
