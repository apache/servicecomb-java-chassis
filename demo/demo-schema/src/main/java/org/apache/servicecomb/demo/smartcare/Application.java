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

package org.apache.servicecomb.demo.smartcare;

import java.util.List;

public class Application {
  private String name;

  private String labelEN;

  private String labelCH;

  private String defaultGroup;

  private String version;

  private boolean dynamicFlag;

  private List<Group> groups;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLabelEN() {
    return labelEN;
  }

  public void setLabelEN(String labelEN) {
    this.labelEN = labelEN;
  }

  public String getLabelCH() {
    return labelCH;
  }

  public void setLabelCH(String labelCH) {
    this.labelCH = labelCH;
  }

  public String getDefaultGroup() {
    return defaultGroup;
  }

  public void setDefaultGroup(String defaultGroup) {
    this.defaultGroup = defaultGroup;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public boolean isDynamicFlag() {
    return dynamicFlag;
  }

  public void setDynamicFlag(boolean dynamicFlag) {
    this.dynamicFlag = dynamicFlag;
  }

  public List<Group> getGroups() {
    return groups;
  }

  public void setGroups(List<Group> groups) {
    this.groups = groups;
  }

  @Override
  public String toString() {
    return "name=" + name + "\n"
        + "version=" + version;
  }
}
