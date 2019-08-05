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

public class RecursiveObjectParam {
  private InnerRecursiveObjectParam innerRecursiveObjectParam;

  private RecursiveObjectParam recursiveObjectParam;

  private long l;

  private String string;

  private Color color;

  public RecursiveObjectParam() {
  }

  public RecursiveObjectParam(
      InnerRecursiveObjectParam innerRecursiveObjectParam,
      RecursiveObjectParam recursiveObjectParam, long l, String string,
      Color color) {
    this.innerRecursiveObjectParam = innerRecursiveObjectParam;
    this.recursiveObjectParam = recursiveObjectParam;
    this.l = l;
    this.string = string;
    this.color = color;
  }

  public InnerRecursiveObjectParam getInnerRecursiveObjectParam() {
    return innerRecursiveObjectParam;
  }

  public void setInnerRecursiveObjectParam(
      InnerRecursiveObjectParam innerRecursiveObjectParam) {
    this.innerRecursiveObjectParam = innerRecursiveObjectParam;
  }

  public RecursiveObjectParam getRecursiveObjectParam() {
    return recursiveObjectParam;
  }

  public void setRecursiveObjectParam(RecursiveObjectParam recursiveObjectParam) {
    this.recursiveObjectParam = recursiveObjectParam;
  }

  public long getL() {
    return l;
  }

  public void setL(long l) {
    this.l = l;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("RecursiveObjectParam{");
    sb.append("innerRecursiveObjectParam=").append(innerRecursiveObjectParam);
    sb.append(", recursiveObjectParam=").append(recursiveObjectParam);
    sb.append(", l=").append(l);
    sb.append(", string='").append(string).append('\'');
    sb.append(", color=").append(color);
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
    RecursiveObjectParam that = (RecursiveObjectParam) o;
    return l == that.l &&
        Objects.equals(innerRecursiveObjectParam, that.innerRecursiveObjectParam) &&
        Objects.equals(recursiveObjectParam, that.recursiveObjectParam) &&
        Objects.equals(string, that.string) &&
        color == that.color;
  }

  @Override
  public int hashCode() {
    return Objects.hash(innerRecursiveObjectParam, recursiveObjectParam, l, string, color);
  }
}
