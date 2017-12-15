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
package io.protostuff.runtime.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.protostuff.Tag;

public class ModelProtostuff {

  //CHECKSTYLE:OFF: magicnumber
  @Tag(1)
  private String destMicroservice;

  @Tag(5)
  private String schemaId;

  @Tag(6)
  private String operationName;

  @Tag(7)
  private Map<String, String> context;

  @Tag(8)
  private Map<String, User> userMap;

  @Tag(9)
  private List<String> list = new ArrayList<>();

  @Tag(10)
  private List<User> userList = new ArrayList<>();

  //CHECKSTYLE:ON

  public String getDestMicroservice() {
    return destMicroservice;
  }

  public void setDestMicroservice(String destMicroservice) {
    this.destMicroservice = destMicroservice;
  }

  public String getSchemaId() {
    return schemaId;
  }

  public void setSchemaId(String schemaId) {
    this.schemaId = schemaId;
  }

  public String getOperationName() {
    return operationName;
  }

  public void setOperationName(String operationName) {
    this.operationName = operationName;
  }

  public Map<String, String> getContext() {
    return context;
  }

  public void setContext(Map<String, String> context) {
    this.context = context;
  }

  public List<String> getList() {
    return list;
  }

  public void setList(List<String> list) {
    this.list = list;
  }

  public Map<String, User> getUserMap() {
    return userMap;
  }

  public void setUserMap(Map<String, User> userMap) {
    this.userMap = userMap;
  }

  public List<User> getUserList() {
    return userList;
  }

  public void setUserList(List<User> userList) {
    this.userList = userList;
  }
}
