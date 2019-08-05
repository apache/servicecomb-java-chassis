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

public class MultiLayerObjectParam2 {
  private String string;

  private double num;

  private FlattenObjectRequest flattenObject;

  public MultiLayerObjectParam2() {
  }

  public MultiLayerObjectParam2(String string, double num,
      FlattenObjectRequest flattenObject) {
    this.string = string;
    this.num = num;
    this.flattenObject = flattenObject;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public double getNum() {
    return num;
  }

  public void setNum(double num) {
    this.num = num;
  }

  public FlattenObjectRequest getFlattenObject() {
    return flattenObject;
  }

  public void setFlattenObject(FlattenObjectRequest flattenObject) {
    this.flattenObject = flattenObject;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("MultiLayerObjectParam2{");
    sb.append("string='").append(string).append('\'');
    sb.append(", num=").append(num);
    sb.append(", flattenObject=").append(flattenObject);
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
    MultiLayerObjectParam2 that = (MultiLayerObjectParam2) o;
    return Double.compare(that.num, num) == 0 &&
        Objects.equals(string, that.string) &&
        Objects.equals(flattenObject, that.flattenObject);
  }

  @Override
  public int hashCode() {
    return Objects.hash(string, num, flattenObject);
  }
}
