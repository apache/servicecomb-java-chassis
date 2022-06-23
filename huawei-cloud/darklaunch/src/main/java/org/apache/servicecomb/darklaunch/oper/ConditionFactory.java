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

package org.apache.servicecomb.darklaunch.oper;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.darklaunch.DarklaunchRule;

public class ConditionFactory {
  public static final String OP_AND = "&&";

  public static final String OP_OR = "||";

  public static final String OP_OR_ESCAPE = "\\|\\|";

  private static final String[] OP_LIST = {">=", "<=", "!=", "=", ">", "<", "~"};

  public static final String SEP_COLON = ",";

  private static String[] split2Part(String str, String sep) {
    int index = str.indexOf(sep);
    if (index > 0) {
      return new String[] {
          str.substring(0, index), str.substring(index + sep.length())
      };
    } else {
      return new String[] {str};
    }
  }

  private static String[] split(String str, String sep) {
    return Arrays.stream(str.split(sep))
            .filter(s -> !StringUtils.isEmpty(s)).toArray(String[]::new);
  }

  public static Condition buildRateCondition(String strCondition) {
    return new LessCondition(DarklaunchRule.PROP_PERCENT, strCondition);
  }

  public static Condition buildCondition(String strCondition, boolean caseInsensitive) {
    if (strCondition.contains(OP_AND)) {
      String[] rules = split(strCondition, OP_AND);
      Condition[] conditions = new Condition[rules.length];
      for (int i = 0; i < conditions.length; i++) {
        conditions[i] = buildGroupConditionItem(rules[i], caseInsensitive);
      }
      return new AndCondition(conditions);
    } else if (strCondition.contains(OP_OR)) {
      String[] rules = split(strCondition, OP_OR_ESCAPE);
      Condition[] conditions = new Condition[rules.length];
      for (int i = 0; i < conditions.length; i++) {
        conditions[i] = buildGroupConditionItem(rules[i], caseInsensitive);
      }
      return new OrCondition(conditions);
    } else {
      return buildGroupConditionItem(strCondition, caseInsensitive);
    }
  }

  private static Condition buildGroupConditionItem(String groupCondition, boolean caseInsensitive) {
    for (int index = 0; index < OP_LIST.length; index++) {
      if (groupCondition.contains(OP_LIST[index])) {
        String[] pairs = split2Part(groupCondition, OP_LIST[index]);
        if (pairs[1].contains(SEP_COLON)) {
          String[] values = split(pairs[1], SEP_COLON);
          Condition[] conditions = new Condition[values.length];
          for (int i = 0; i < values.length; i++) {
            conditions[i] = buildCondition(index, pairs[0], values[i], caseInsensitive);
          }
          return new OrCondition(conditions);
        } else {
          return buildCondition(index, pairs[0], pairs[1], caseInsensitive);
        }
      }
    }
    throw new IllegalArgumentException(groupCondition);
  }

  private static Condition buildCondition(int index, String key, String value, boolean caseInsensitive) {
    value = caseInsensitive ? value.toLowerCase() : value;
    Condition condition = buildCondition(index, key, value);
    if (caseInsensitive) {
      return new CaseInsensitiveCondition(condition);
    }
    return condition;
  }

  private static Condition buildCondition(int index, String key, String value) {
    switch (index) {
      case 0:
        return new GreaterOrEqualCondition(key, value);
      case 1:
        return new LessOrEqualCondition(key, value);
      case 2:
        return new NotEqualCondition(key, value);
      case 3:
        return new EqualCondition(key, value);
      case 4:
        return new GreaterCondition(key, value);
      case 5:
        return new LessCondition(key, value);
      default:
        return new LikeCondition(key, value);
    }
  }
}
