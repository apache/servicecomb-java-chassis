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

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.HashMap;
import java.util.Map;

public class LabelDocResponse {

  @JsonAlias("label_id")
  private String labelId;

  private Map<String, String> labels = new HashMap<>();

  public String getLabelId() {
    return labelId;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  public void setLabelId(String labelId) {
    this.labelId = labelId;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }
}
