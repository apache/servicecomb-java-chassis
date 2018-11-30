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

package org.apache.servicecomb.demo.compute;

public class GenericParam<T> {
  private String str;

  private long num;

  private T data;

  public String getStr() {
    return str;
  }

  public GenericParam<T> setStr(String str) {
    this.str = str;
    return this;
  }

  public long getNum() {
    return num;
  }

  public GenericParam<T> setNum(long num) {
    this.num = num;
    return this;
  }

  public T getData() {
    return data;
  }

  public GenericParam<T> setData(T data) {
    this.data = data;
    return this;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("GenericParam{");
    sb.append("str='").append(str).append('\'');
    sb.append(", num=").append(num);
    sb.append(", data=").append(data);
    sb.append('}');
    return sb.toString();
  }
}
