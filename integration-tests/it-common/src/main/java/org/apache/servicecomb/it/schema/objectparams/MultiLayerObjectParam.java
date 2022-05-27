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

import java.util.Date;
import java.util.Objects;

public class MultiLayerObjectParam {
  private String string;

  private Date date;

  private MultiLayerObjectParam2 param2;

  public MultiLayerObjectParam() {
  }

  public MultiLayerObjectParam(String string, Date date,
      MultiLayerObjectParam2 param2) {
    this.string = string;
    this.date = date;
    this.param2 = param2;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public MultiLayerObjectParam2 getParam2() {
    return param2;
  }

  public void setParam2(MultiLayerObjectParam2 param2) {
    this.param2 = param2;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("MultiLayerObjectParam{");
    sb.append("string='").append(string).append('\'');
    sb.append(", date=").append(date);
    sb.append(", param2=").append(param2);
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
    MultiLayerObjectParam that = (MultiLayerObjectParam) o;
    return Objects.equals(string, that.string) &&
        Objects.equals(date, that.date) &&
        Objects.equals(param2, that.param2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(string, date, param2);
  }
}
