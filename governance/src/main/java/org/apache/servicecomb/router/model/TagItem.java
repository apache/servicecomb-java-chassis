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
package org.apache.servicecomb.router.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class TagItem {

  private static final String VERSION = "version";

  private String version;

  private Map<String, String> param;

  public TagItem() {
  }

  public TagItem(String version, Map<String, String> param) {
    this.version = version;
    this.param = param;
  }

  public TagItem(String version) {
    this.version = version;
    Map<String, String> param = new HashMap<>();
    param.put(VERSION, version);
    this.param = param;
  }

  public TagItem(Map<String, String> param) {
    if (param.containsKey(VERSION)) {
      this.version = param.get(VERSION);
    }
    this.param = param;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Map<String, String> getParam() {
    return param;
  }

  public void setParam(Map<String, String> param) {
    this.param = param;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getVersion(), getParam());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TagItem)) {
      return false;
    }
    TagItem tagItem = (TagItem) o;
    return Objects.equals(getVersion(), tagItem.getVersion()) &&
        Objects.equals(getParam(), tagItem.getParam());
  }

  /**
   * return match num
   *
   * @param item
   * @return
   */
  public int matchNum(TagItem item) {
    int cnt = 0;
    if (version != null && !version.equals(item.version)) {
      return 0;
    }
    for (Map.Entry<String, String> entry : param.entrySet()) {
      if (item.getParam().containsKey(entry.getKey()) &&
          !item.getParam().get(entry.getKey()).equals(entry.getValue())) {
        return 0;
      }
      cnt++;
    }
    return cnt;
  }
}
