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

package org.apache.servicecomb.config.inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.servicecomb.config.ConfigUtil;

/**
 * <pre>
 * not care for performance
 *
 * behavior of multiple list if defined:
 *   org.apache.servicecomb.config.inject.TestPlaceholderResolver#multi_list()
 * behavior of nested and multiple list variable is undefined
 *   org.apache.servicecomb.config.inject.TestPlaceholderResolver#mixed()
 * </pre>
 */
public class PlaceholderResolver {
  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("(?<escape>\\\\)?\\$\\{(?<name>[^{}]+)\\}");

  static class SplitPart {
    boolean var;

    String fullName;

    Object value;

    public SplitPart(boolean var, String fullName) {
      this.var = var;
      this.fullName = fullName;
    }

    @Override
    public String toString() {
      return "SplitPart{" +
          "var=" + var +
          ", fullName='" + fullName + '\'' +
          ", value=" + value +
          '}';
    }
  }

  static class Row {
    List<SplitPart> parts = new ArrayList<>();

    int cartesianProductCount = 1;

    int varCount = 0;
  }

  public String replaceFirst(String str) {
    return replace(str, Collections.emptyMap()).get(0);
  }

  public String replaceFirst(String str, Map<String, Object> parameters) {
    return replace(str, parameters).get(0);
  }

  public List<String> replace(String str, Map<String, Object> parameters) {
    List<Row> finalRows = replaceToRows(str, parameters);

    List<String> replaced = new ArrayList<>();
    for (Row row : finalRows) {
      resolve(row, replaced);
    }

    for (int idx = 0; idx < replaced.size(); idx++) {
      String row = replaced.get(idx);
      replaced.set(idx, row.replace("\\$", "$"));
    }
    return replaced;
  }

  private List<Row> replaceToRows(String str, Map<String, Object> parameters) {
    List<Row> finalRows = new ArrayList<>();
    List<String> remainRows = new ArrayList<>();
    replaceToRows(str, parameters, remainRows, finalRows);

    for (String row : remainRows) {
      List<Row> nestedRows = replaceToRows(row, parameters);
      finalRows.addAll(nestedRows);
    }
    return finalRows;
  }

  private void replaceToRows(String str, Map<String, Object> parameters, List<String> remainRows,
      List<Row> finalRows) {
    Row row = parseToRow(str, parameters);
    if (row.varCount == 0 && row.cartesianProductCount == 1) {
      finalRows.add(row);
      return;
    }

    resolve(row, remainRows);
  }

  private Row parseToRow(String str, Map<String, Object> parameters) {
    Matcher matcher = PLACEHOLDER_PATTERN.matcher(str);
    Row row = new Row();

    int last = 0;
    while (matcher.find()) {
      row.parts.add(new SplitPart(false, str.substring(last, matcher.start())));
      last = matcher.end();

      if (matcher.group("escape") != null) {
        row.parts.add(new SplitPart(false, matcher.group().substring(1)));
        continue;
      }

      String name = matcher.group("name");
      Object value = findValue(parameters, name);
      if (value instanceof Collection) {
        row.cartesianProductCount *= ((Collection) value).size();
      }
      if (value != null) {
        row.varCount++;
      }

      SplitPart splitPart = new SplitPart(value != null, matcher.group());
      splitPart.value = value;
      row.parts.add(splitPart);
    }
    row.parts.add(new SplitPart(false, str.substring(last)));

    return row;
  }

  // resolve placeholder and execute cartesian product
  @SuppressWarnings("unchecked")
  private void resolve(Row row, List<String> resolvedRows) {
    List<StringBuilder> stringBuilders = new ArrayList<>();
    for (int idx = 0; idx < row.cartesianProductCount; idx++) {
      stringBuilders.add(new StringBuilder());
    }

    int collectionRepeatCount = 1;
    for (SplitPart part : row.parts) {
      if (!part.var) {
        for (int idx = 0; idx < row.cartesianProductCount; idx++) {
          StringBuilder sb = stringBuilders.get(idx);

          if (part.fullName.startsWith("$")) {
            sb.append("\\" + part.fullName);
            continue;
          }

          sb.append(part.fullName);
        }
        continue;
      }

      if (part.value instanceof Collection) {
        int size = ((Collection<String>) part.value).size();
        int rowRepeatCount = row.cartesianProductCount / size / collectionRepeatCount;

        int valueIdx = 0;
        for (int collectionRepeatIdx = 0; collectionRepeatIdx < collectionRepeatCount; collectionRepeatIdx++) {
          for (String value : (Collection<String>) part.value) {
            for (int repeatIdx = 0; repeatIdx < rowRepeatCount; repeatIdx++) {
              StringBuilder sb = stringBuilders.get(valueIdx);
              valueIdx++;
              sb.append(value);
            }
          }
        }

        collectionRepeatCount *= size;
        continue;
      }

      // normal var
      for (int idx = 0; idx < row.cartesianProductCount; idx++) {
        StringBuilder sb = stringBuilders.get(idx);
        sb.append(part.value);
      }
    }

    for (StringBuilder sb : stringBuilders) {
      resolvedRows.add(sb.toString());
    }
  }

  private Object findValue(Map<String, Object> parameters, String key) {
    Object value = parameters.get(key);
    if (value == null) {
      value = ConfigUtil.getProperty(key);
    }
    return value;
  }
}