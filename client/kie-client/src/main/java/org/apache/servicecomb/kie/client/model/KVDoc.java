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

public class KVDoc {

  private String _id;

  private String check;

  private String domain;

  private String key;

  private String label_id;

  private Map<String, String> labels = new HashMap<String, String>();

  private Integer revision;

  private String value;

  private String value_type;

  public String get_id() {
    return _id;
  }

  public void set_id(String _id) {
    this._id = _id;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getCheck() {
    return check;
  }

  public String getDomain() {
    return domain;
  }

  public String getLabel_id() {
    return label_id;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  public Integer getRevision() {
    return revision;
  }

  public String getValue() {
    return value;
  }

  public void setCheck(String check) {
    this.check = check;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public void setLabel_id(String label_id) {
    this.label_id = label_id;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }

  public void setRevision(Integer revision) {
    this.revision = revision;
  }

  public void setValue_type(String value_type) {
    this.value_type = value_type;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getValue_type() {
    return value_type;
  }
}
