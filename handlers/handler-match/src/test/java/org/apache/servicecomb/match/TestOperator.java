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
package org.apache.servicecomb.match;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.match.marker.operator.CompareOperator;
import org.apache.servicecomb.match.marker.operator.ContainsOperator;
import org.apache.servicecomb.match.marker.operator.ExactOperator;
import org.apache.servicecomb.match.marker.operator.MatchOperator;
import org.apache.servicecomb.match.marker.operator.RegexOperator;
import org.junit.Assert;
import org.junit.Test;

public class TestOperator {

  private Map<String, MatchOperator> operatorMap;

  private static final String REGEX_KEY = "regex";

  private static final String EXACT_KEY = "exact";

  private static final String CONTAINS_KEY = "contains";

  private static final String COMPARE_KEY = "compare";

  {
    operatorMap = new HashMap<>();
    operatorMap.put(REGEX_KEY, new RegexOperator());
    operatorMap.put(EXACT_KEY, new ExactOperator());
    operatorMap.put(CONTAINS_KEY, new ContainsOperator());
    operatorMap.put(COMPARE_KEY, new CompareOperator());
  }

  @Test
  public void testRegex() {
    String patternStr = "/.*";
    String targetStr1 = "/xxxx/xxx1";
    String targetStr2 = "xxxx/xxx1";
    Assert.assertTrue(operatorMap.get(REGEX_KEY).match(targetStr1, patternStr));
    Assert.assertFalse(operatorMap.get(REGEX_KEY).match(targetStr2, patternStr));
  }

  @Test
  public void testContain() {
    String patternStr = "/xxx";
    String targetStr1 = "/xxxx/xxx1";
    String targetStr2 = "/12344";
    Assert.assertTrue(operatorMap.get(CONTAINS_KEY).match(targetStr1, patternStr));
    Assert.assertFalse(operatorMap.get(CONTAINS_KEY).match(targetStr2, patternStr));
  }

  @Test
  public void testCompare() {
    String patternStr = ">123";
    String targetStr1 = "133";
    String targetStr2 = "90";
    Assert.assertTrue(operatorMap.get(COMPARE_KEY).match(targetStr1, patternStr));
    Assert.assertFalse(operatorMap.get(COMPARE_KEY).match(targetStr2, patternStr));

    String patternStr2 = ">=123";
    String targetStr3 = "123";
    Assert.assertTrue(operatorMap.get(COMPARE_KEY).match(targetStr3, patternStr2));
    Assert.assertFalse(operatorMap.get(COMPARE_KEY).match(targetStr2, patternStr2));

    String patternStr3 = "=123";
    Assert.assertTrue(operatorMap.get(COMPARE_KEY).match(targetStr3, patternStr3));
    Assert.assertFalse(operatorMap.get(COMPARE_KEY).match(targetStr2, patternStr3));

    String patternStr4 = "!123";
    Assert.assertFalse(operatorMap.get(COMPARE_KEY).match(targetStr3, patternStr4));
    Assert.assertTrue(operatorMap.get(COMPARE_KEY).match(targetStr2, patternStr4));

    String patternStr5 = "<=-123";
    String targetStr4 = "-123";
    Assert.assertFalse(operatorMap.get(COMPARE_KEY).match(targetStr3, patternStr5));
    Assert.assertTrue(operatorMap.get(COMPARE_KEY).match(targetStr4, patternStr5));
  }

  @Test
  public void testExact() {
    String patternStr = "/xxx";
    String targetStr1 = "/xxx";
    String targetStr2 = "/12344";
    Assert.assertTrue(operatorMap.get(EXACT_KEY).match(targetStr1, patternStr));
    Assert.assertFalse(operatorMap.get(EXACT_KEY).match(targetStr2, patternStr));
  }
}
