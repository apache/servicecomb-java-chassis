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

public class GenericParamExtended<T> extends GenericParam<T> {
  private String strExtended;

  private int intExtended;

  public String getStrExtended() {
    return strExtended;
  }

  public GenericParamExtended<T> setStrExtended(String strExtended) {
    this.strExtended = strExtended;
    return this;
  }

  public int getIntExtended() {
    return intExtended;
  }

  public GenericParamExtended<T> setIntExtended(int intExtended) {
    this.intExtended = intExtended;
    return this;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("GenericParamExtended{");
    sb.append("strExtended='").append(strExtended).append('\'');
    sb.append(", intExtended=").append(intExtended);
    sb.append(", super=").append(super.toString());
    sb.append('}');
    return sb.toString();
  }
}
