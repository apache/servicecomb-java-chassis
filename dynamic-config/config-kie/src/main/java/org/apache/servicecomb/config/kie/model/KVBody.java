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

package org.apache.servicecomb.config.kie.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.HashMap;
import java.util.Map;
import org.apache.servicecomb.config.kie.client.KieConfig;

public class KVBody {

  private Map<String, String> labels = new HashMap<String, String>();

  private String value;

  @JsonAlias("value_type")
  private String valueType;

  public Map<String, String> getLabels() {
    return labels;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }

  public void initLabels() {
    labels.put("env", KieConfig.INSTANCE.getEnvironment());
    labels.put("app", KieConfig.INSTANCE.getAppName());
    labels.put("service", KieConfig.INSTANCE.getServiceName());
    labels.put("version", KieConfig.INSTANCE.getVersion());
    labels.put("tags", KieConfig.INSTANCE.getTags());
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getValueType() {
    return valueType;
  }

  public void setValueType(String valueType) {
    this.valueType = valueType;
  }
}
