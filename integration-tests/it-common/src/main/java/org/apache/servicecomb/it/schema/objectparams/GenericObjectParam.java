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

package org.apache.servicecomb.it.schema.objectparams;

import java.util.Objects;

public class GenericObjectParam<T> {
  private String string;
  private int i;
  private T obj;

  public GenericObjectParam() {
  }

  public GenericObjectParam(String string, int i, T obj) {
    this.string = string;
    this.i = i;
    this.obj = obj;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public int getI() {
    return i;
  }

  public void setI(int i) {
    this.i = i;
  }

  public T getObj() {
    return obj;
  }

  public void setObj(T obj) {
    this.obj = obj;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GenericObjectParam<?> that = (GenericObjectParam<?>) o;
    return i == that.i &&
        Objects.equals(string, that.string) &&
        Objects.equals(obj, that.obj);
  }

  @Override
  public int hashCode() {
    return Objects.hash(string, i, obj);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("GenericObjectParam{");
    sb.append("string='").append(string).append('\'');
    sb.append(", i=").append(i);
    sb.append(", obj=").append(obj);
    sb.append('}');
    return sb.toString();
  }
}
