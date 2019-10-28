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

package org.apache.servicecomb.kie.client.model;

import java.util.HashMap;
import java.util.Map;

public class LabelDocResponse {

  private String label_id;

  private Map<String, String> labels = new HashMap<String, String>();

  public String getLabel_id() {
    return label_id;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  public void setLabel_id(String label_id) {
    this.label_id = label_id;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }
}
