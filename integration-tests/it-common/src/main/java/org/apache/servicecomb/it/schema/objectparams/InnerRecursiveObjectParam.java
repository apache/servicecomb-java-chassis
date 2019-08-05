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

public class InnerRecursiveObjectParam {
  private int i;

  private String string;

  private RecursiveObjectParam recursiveObjectParam;

  public InnerRecursiveObjectParam() {
  }

  public InnerRecursiveObjectParam(int i, String string,
      RecursiveObjectParam recursiveObjectParam) {
    this.i = i;
    this.string = string;
    this.recursiveObjectParam = recursiveObjectParam;
  }

  public int getI() {
    return i;
  }

  public void setI(int i) {
    this.i = i;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public RecursiveObjectParam getRecursiveObjectParam() {
    return recursiveObjectParam;
  }

  public void setRecursiveObjectParam(RecursiveObjectParam recursiveObjectParam) {
    this.recursiveObjectParam = recursiveObjectParam;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("InnerRecursiveObjectParam{");
    sb.append("i=").append(i);
    sb.append(", string='").append(string).append('\'');
    sb.append(", recursiveObjectParam=").append(recursiveObjectParam);
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InnerRecursiveObjectParam that = (InnerRecursiveObjectParam) o;
    return i == that.i &&
        Objects.equals(string, that.string) &&
        Objects.equals(recursiveObjectParam, that.recursiveObjectParam);
  }

  @Override
  public int hashCode() {
    return Objects.hash(i, string, recursiveObjectParam);
  }
}
