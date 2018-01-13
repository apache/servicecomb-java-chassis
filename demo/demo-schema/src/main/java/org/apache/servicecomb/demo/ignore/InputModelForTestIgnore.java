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

package org.apache.servicecomb.demo.ignore;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.vertx.core.json.JsonObject;

public class InputModelForTestIgnore {
  @JsonIgnore
  private String inputId = null;
  private String content = null;

  @JsonIgnore
  private Object inputObject = null;
  @JsonIgnore
  private JsonObject inputJsonObject = null;
  @JsonIgnore
  private IgnoreInterface inputIgnoreInterface = null;

  public String getInputId() {
    return this.inputId;
  }

  public void setInputId(String inputId) {
    this.inputId = inputId;
  }

  public String getContent() {
    return this.content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Object getInputObject() {
    return inputObject;
  }

  public void setInputObject(Object inputObject) {
    this.inputObject = inputObject;
  }

  public JsonObject getInputJsonObject() {
    return inputJsonObject;
  }

  public void setInputJsonObject(JsonObject inputJsonObject) {
    this.inputJsonObject = inputJsonObject;
  }

  public IgnoreInterface getInputIgnoreInterface() {
    return inputIgnoreInterface;
  }

  public void setInputIgnoreInterface(IgnoreInterface inputIgnoreInterface) {
    this.inputIgnoreInterface = inputIgnoreInterface;
  }

  public InputModelForTestIgnore() {
  }

  public InputModelForTestIgnore(String inputId, String content, Object inputObject,
      JsonObject inputJsonObject, IgnoreInterface inputIgnoreInterface) {
    this.inputId = inputId;
    this.content = content;
    this.inputObject = inputObject;
    this.inputJsonObject = inputJsonObject;
    this.inputIgnoreInterface = inputIgnoreInterface;
  }
}
