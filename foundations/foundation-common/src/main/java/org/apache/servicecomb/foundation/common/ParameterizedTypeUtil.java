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
package org.apache.servicecomb.foundation.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Utility class for creating ParameterizedType.
 */
public class ParameterizedTypeUtil implements ParameterizedType {
  private final Type[] actualTypeArguments;

  private final Class<?> rawType;

  private ParameterizedTypeUtil(Class<?> rawType, Type[] actualTypeArguments) {
    this.actualTypeArguments = actualTypeArguments;
    this.rawType = rawType;
  }

  public static ParameterizedType make(Class<?> rawType, Type... actualTypeArguments) {
    return new ParameterizedTypeUtil(rawType, actualTypeArguments);
  }

  @Override
  public Type[] getActualTypeArguments() {
    return this.actualTypeArguments;
  }

  @Override
  public Type getRawType() {
    return rawType;
  }

  @Override
  public Type getOwnerType() {
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ParameterizedType that) {

      if (this == that) {
        return true;
      }

      Type thatRawType = that.getRawType();

      return Objects.equals(rawType, thatRawType) &&
          Arrays.equals(actualTypeArguments, that.getActualTypeArguments());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(actualTypeArguments) ^
        Objects.hashCode(rawType);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append(rawType.getName());

    if (actualTypeArguments != null) {
      StringJoiner sj = new StringJoiner(", ", "<", ">");
      sj.setEmptyValue("");
      for (Type t : actualTypeArguments) {
        sj.add(t.getTypeName());
      }
      sb.append(sj);
    }

    return sb.toString();
  }
}
