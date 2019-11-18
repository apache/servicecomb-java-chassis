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

import org.apache.servicecomb.router.exception.RouterIllegalParamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class HeaderRule {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeaderRule.class);

  //正则
  private String regex;
  //是否区分大小写 false区分 true不区分
  private Boolean caseInsensitive = false;
  //精准匹配
  private String exact;

  public HeaderRule() {
  }

  public boolean match(String str) {
    if (str == null) {
      return false;
    }
    if (exact == null && regex == null) {
      throw new RouterIllegalParamException(
          "route management regex and exact can not br null at same time.");
    }
    if (!caseInsensitive) {
      str = str.toLowerCase();
      exact = exact == null ? null : exact.toLowerCase();
      regex = regex == null ? null : regex.toLowerCase();
    }
    if (exact != null && !str.equals(exact)) {
      return false;
    }
    try {
      if (regex != null && !str.matches(regex)) {
        return false;
      }
    } catch (Exception e) {
      LOGGER.error("route management wrong regular expression format: {}", regex);
      return false;
    }
    return true;
  }

  public String getRegex() {
    return regex;
  }

  public void setRegex(String regex) {
    this.regex = regex;
  }

  public Boolean getCaseInsensitive() {
    return caseInsensitive;
  }

  public void setCaseInsensitive(Boolean caseInsensitive) {
    this.caseInsensitive = caseInsensitive;
  }

  public String getExact() {
    return exact;
  }

  public void setExact(String exact) {
    this.exact = exact;
  }

  @Override
  public String toString() {
    return "HeaderRule{" +
        "regex='" + regex + '\'' +
        ", caseInsensitive=" + caseInsensitive +
        ", exact='" + exact + '\'' +
        '}';
  }
}
