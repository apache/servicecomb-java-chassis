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

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;

public class AccessLogElementExtraction {
  private int start;

  private int end;

  private AccessLogElement accessLogElement;

  public AccessLogElementExtraction() {

  }

  public AccessLogElementExtraction(int start, int end,
      AccessLogElement accessLogElement) {
    this.start = start;
    this.end = end;
    this.accessLogElement = accessLogElement;
  }

  public int getStart() {
    return start;
  }

  public AccessLogElementExtraction setStart(int start) {
    this.start = start;
    return this;
  }

  public int getEnd() {
    return end;
  }

  public AccessLogElementExtraction setEnd(int end) {
    this.end = end;
    return this;
  }

  public AccessLogElement getAccessLogElement() {
    return accessLogElement;
  }

  public AccessLogElementExtraction setAccessLogElement(
      AccessLogElement accessLogElement) {
    this.accessLogElement = accessLogElement;
    return this;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("AccessLogElementExtraction{");
    sb.append("start=").append(start);
    sb.append(", end=").append(end);
    sb.append(", accessLogElement=").append(accessLogElement);
    sb.append('}');
    return sb.toString();
  }
}
