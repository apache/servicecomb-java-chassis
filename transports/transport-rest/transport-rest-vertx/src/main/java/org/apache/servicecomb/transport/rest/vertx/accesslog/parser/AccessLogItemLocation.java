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

package org.apache.servicecomb.transport.rest.vertx.accesslog.parser;

import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;

import com.google.common.base.Objects;

public class AccessLogItemLocation {
  private int start;

  private int end;

  private AccessLogItemTypeEnum placeHolder;

  public int getStart() {
    return start;
  }

  public AccessLogItemLocation setStart(int start) {
    this.start = start;
    return this;
  }

  public int getEnd() {
    return end;
  }

  public AccessLogItemLocation setEnd(int end) {
    this.end = end;
    return this;
  }

  public AccessLogItemTypeEnum getPlaceHolder() {
    return placeHolder;
  }

  public AccessLogItemLocation setPlaceHolder(AccessLogItemTypeEnum placeHolder) {
    this.placeHolder = placeHolder;
    return this;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("AccessLogItemLocation{");
    sb.append("start=").append(start);
    sb.append(", end=").append(end);
    sb.append(", placeHolder=").append(placeHolder);
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !getClass().isAssignableFrom(o.getClass())) {
      return false;
    }
    AccessLogItemLocation that = (AccessLogItemLocation) o;
    return start == that.start
        && end == that.end
        && placeHolder == that.placeHolder;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(start, end, placeHolder);
  }
}
