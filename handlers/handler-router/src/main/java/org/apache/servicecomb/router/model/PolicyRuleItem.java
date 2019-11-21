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
import org.apache.servicecomb.router.exception.RouterIllegalParamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class PolicyRuleItem implements Comparable<PolicyRuleItem> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PolicyRuleItem.class);

  private Integer precedence;

  private Matcher match;

  // any match
  private List<RouteItem> route;

  private Integer total;

  private boolean weightLess = false;

  public PolicyRuleItem() {
  }

  /**
   * if weight is less than 100, fill with minimum version
   *
   * @param latestVersionTag
   */
  public void check(TagItem latestVersionTag) {
    if (CollectionUtils.isEmpty(route)) {
      throw new RouterIllegalParamException("canary rule list can not be null");
    }
    if (route.size() == 1) {
      route.get(0).setWeight(100);
      return;
    }
    int sum = 0;
    for (RouteItem item : route) {
      if (item.getWeight() == null) {
        throw new RouterIllegalParamException("canary rule weight can not be null");
      }
      sum += item.getWeight();
    }
    if (sum > 100) {
      LOGGER.warn("canary rule weight sum is more than 100");
    } else if (sum < 100) {
      if (latestVersionTag == null) {
        LOGGER.warn("canary has some error when set default latestVersion");
      }
      weightLess = true;
      route.add(new RouteItem(100 - sum, latestVersionTag));
    }
  }

  @Override
  public int compareTo(PolicyRuleItem param) {
    if (param.precedence.equals(this.precedence)) {
      LOGGER.warn("the same canary precedence is not recommended");
      return 0;
    }
    return param.precedence > this.precedence ? 1 : -1;
  }

  public Integer getPrecedence() {
    return precedence;
  }

  public void setPrecedence(Integer precedence) {
    this.precedence = precedence;
  }

  public Matcher getMatch() {
    return match;
  }

  public void setMatch(Matcher match) {
    this.match = match;
  }

  public List<RouteItem> getRoute() {
    return route;
  }

  public void setRoute(List<RouteItem> route) {
    this.route = route;
  }

  public Integer getTotal() {
    return total;
  }

  public void setTotal(Integer total) {
    this.total = total;
  }

  public boolean isWeightLess() {
    return weightLess;
  }

  public void setWeightLess(boolean weightLess) {
    this.weightLess = weightLess;
  }

  @Override
  public String toString() {
    return "PolicyRuleItem{" +
        "precedence=" + precedence +
        ", match=" + match +
        ", route=" + route +
        ", total=" + total +
        ", weightLess=" + weightLess +
        '}';
  }
}
