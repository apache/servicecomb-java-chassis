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

import java.util.Map;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class RouteItem implements Comparable<RouteItem> {

  private Integer weight;
  /**
   * 负载均衡参数
   */
  private Integer currentWeight = 0;
  /**
   * 为了提高序列化速度设置为Map 固定字段 version
   */
  private Map<String, String> tags;

  private TagItem tagitem;


  public void initTagItem() {
    if (tags != null && tags.containsKey("version")) {
      tagitem = new TagItem(tags);
    }
  }

  public void addCurrentWeight() {
    currentWeight += weight;
  }

  public void reduceCurrentWeight(int total) {
    currentWeight -= total;
  }

  public RouteItem() {
  }

  public RouteItem(Integer weight, TagItem tags) {
    this.weight = weight;
    this.tagitem = tags;
  }

  public Integer getWeight() {
    return weight;
  }

  public void setWeight(Integer weight) {
    this.weight = weight;
  }

  public Integer getCurrentWeight() {
    return currentWeight;
  }

  public void setCurrentWeight(Integer currentWeight) {
    this.currentWeight = currentWeight;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public void setTags(Map<String, String> tags) {
    this.tags = tags;
  }

  public TagItem getTagitem() {
    return tagitem;
  }

  public void setTagitem(TagItem tagitem) {
    this.tagitem = tagitem;
  }

  @Override
  public int compareTo(RouteItem param) {
    if (param.weight == this.weight) {
      return 0;
    }
    return param.weight > this.weight ? 1 : -1;
  }

  @Override
  public String toString() {
    return "RouteItem{" +
        "weight=" + weight +
        ", currentWeight=" + currentWeight +
        ", tags=" + tags +
        ", tagitem=" + tagitem +
        '}';
  }
}
