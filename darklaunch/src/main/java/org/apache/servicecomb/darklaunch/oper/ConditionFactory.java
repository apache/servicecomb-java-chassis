package org.apache.servicecomb.darklaunch.oper;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.darklaunch.DarklaunchRule;

import rx.Completable.CompletableOnSubscribe;

public class ConditionFactory {
  public static final String OP_AND = "&&";

  public static final String OP_OR = "||";

  public static final String OP_OR_ESCAPE = "\\|\\|";

  private static final String[] OP_LIST = {">=", "<=", "!=", "=", ">", "<", "~"};

  public static final String SEP_CPLON = ",";

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
        .filter(s -> !StringUtils.isEmpty(s))
        .collect(Collectors.toList())
        .toArray(new String[0]);
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
        if (pairs[1].contains(SEP_CPLON)) {
          String[] values = split(pairs[1], SEP_CPLON);
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

  public static Condition buildCondition(int index, String key, String value, boolean caseInsensitive) {
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
