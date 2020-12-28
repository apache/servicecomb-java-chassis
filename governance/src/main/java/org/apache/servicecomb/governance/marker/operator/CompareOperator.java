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
package org.apache.servicecomb.governance.marker.operator;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import org.apache.servicecomb.governance.exception.IllegalArgsOperatorException;


@Component
public class CompareOperator implements MatchOperator {

  private Set<Character> charSet = new HashSet<>();

  public CompareOperator() {
    charSet.add('>');
    charSet.add('<');
    charSet.add('=');
    charSet.add('!');
  }

  /**
   * 支持 > < = >= <= ! 后面加数字
   *
   * @param targetStr
   * @param patternStr
   * @return
   */
  @Override
  public boolean match(String targetStr, String patternStr) {
    char[] chars = patternStr.toCharArray();
    if (isLegalChar(chars[0]) && isLegalChar(chars[1])) {
      return process(targetStr, patternStr.substring(0, 2), patternStr.substring(2));
    } else if (isLegalChar(chars[0])) {
      return process(targetStr, patternStr.substring(0, 1), patternStr.substring(1));
    } else {
      throw new IllegalArgsOperatorException("operator " + patternStr + " is illegal.");
    }
  }

  private boolean process(String targetStr, String charStr, String numStr) {
    double result;
    double target;
    try {
      target = Double.parseDouble(targetStr);
      if (numStr.startsWith("-")) {
        result = -Double.parseDouble(numStr.substring(1));
      } else {
        result = Double.parseDouble(numStr);
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgsOperatorException("operator " + charStr + numStr + " is illegal.");
    }
    switch (charStr) {
      case ">":
        return target > result;
      case "<":
        return target < result;
      case "=":
        return doubleEquals(target, result);
      case ">=":
        return target >= result;
      case "<=":
        return target <= result;
      case "!":
      case "!=":
        return !doubleEquals(target, result);
      default:
        throw new IllegalArgsOperatorException("operator " + charStr + numStr + " is illegal.");
    }
  }

  private boolean isLegalChar(char c) {
    return charSet.contains(c);
  }

  private boolean doubleEquals(double target, double result) {
    return Math.abs(target - result) < 1e-6;
  }
}
