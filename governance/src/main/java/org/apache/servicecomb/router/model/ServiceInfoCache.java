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

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class ServiceInfoCache {

  private List<PolicyRuleItem> allrule;

  /**
   * for default version
   */
  private TagItem latestVersionTag;

  public ServiceInfoCache() {
  }

  public ServiceInfoCache(List<PolicyRuleItem> policyRuleItemList) {
    this.setAllrule(policyRuleItemList);
    // init tagitem
    this.getAllrule().forEach(rule ->
        rule.getRoute().forEach(RouteItem::initTagItem)
    );
    // sort by precedence
    this.sortRule();
  }

  public void sortRule() {
    allrule = allrule.stream().sorted().collect(Collectors.toList());
  }

  public TagItem getNextInvokeVersion(PolicyRuleItem policyRuleItem) {
    List<RouteItem> rule = policyRuleItem.getRoute();
    if (policyRuleItem.getTotal() == null) {
      policyRuleItem.setTotal(rule.stream().mapToInt(RouteItem::getWeight).sum());
    }
    rule.stream().forEach(RouteItem::addCurrentWeight);
    int maxIndex = 0, maxWeight = -1;
    for (int i = 0; i < rule.size(); i++) {
      if (maxWeight < rule.get(i).getCurrentWeight()) {
        maxIndex = i;
        maxWeight = rule.get(i).getCurrentWeight();
      }
    }
    rule.get(maxIndex).reduceCurrentWeight(policyRuleItem.getTotal());
    return rule.get(maxIndex).getTagitem();
  }

  public List<PolicyRuleItem> getAllrule() {
    return allrule;
  }

  public void setAllrule(List<PolicyRuleItem> allrule) {
    this.allrule = allrule;
  }

  public TagItem getLatestVersionTag() {
    return latestVersionTag;
  }

  public void setLatestVersionTag(TagItem latestVersionTag) {
    this.latestVersionTag = latestVersionTag;
  }

  @Override
  public String toString() {
    return "ServiceInfoCache{" +
        "allrule=" + allrule +
        ", latestVersionTag=" + latestVersionTag +
        '}';
  }
}
