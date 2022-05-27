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

public abstract class AbstractCondition implements Condition {
  private final String key;

  private final String expected;

  private Object actual;

  private SupportedType type = SupportedType.UNKNOWN;

  public AbstractCondition(String key, String expected) {
    assertValueNotNull(key, expected);
    this.key = key;
    this.expected = expected;
  }

  @Override
  public String key() {
    return this.key;
  }

  @Override
  public String expected() {
    return this.expected;
  }

  @Override
  public void setActual(String key, Object actual) {
    assertValueNotNull(key, "");
    if (this.key.equals(key)) {
      if (actual instanceof String) {
        this.type = SupportedType.STRING;
      } else if (actual instanceof Number) {
        this.type = SupportedType.NUMBER;
      }
      this.actual = actual;
    }
  }

  protected void assertValueNotNull(String key, Object value) {
    if (key == null) {
      throw new IllegalArgumentException("Key can not be null.");
    }
    if (value == null) {
      throw new IllegalArgumentException("Argument can not be null. key = " + key);
    }
  }

  public static int compareNum(Object num, String anotherNum) {
    try {
      if (num instanceof Integer) {
        return Integer.compare((Integer) num, Integer.parseInt(anotherNum));
      }
      if (num instanceof Long) {
        return Long.compare((Long) num, Long.parseLong(anotherNum));
      }
      if (num instanceof Double) {
        return Double.compare((Double) num, Double.parseDouble(anotherNum));
      }
      if (num instanceof Float) {
        return Float.compare((Float) num, Float.parseFloat(anotherNum));
      }
    } catch (NumberFormatException e) {
      return 1;
    }
    return 1;
  }

  public Object getActual() {
    return actual;
  }

  public SupportedType getType() {
    return type;
  }
}
