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

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItem;

/**
 * The meta data of {@linkplain AccessLogItem}.
 */
public class AccessLogItemMeta<T> {
  protected String prefix;

  protected String suffix;

  /**
   * Used for sorting {@linkplain AccessLogItemMeta}. Default value is 0.
   * Smaller one has higher priority.
   */
  protected int order;

  protected AccessLogItemCreator<T> accessLogItemCreator;

  public String getPrefix() {
    return prefix;
  }

  public AccessLogItemMeta<T> setPrefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

  public String getSuffix() {
    return suffix;
  }

  public AccessLogItemMeta<T> setSuffix(String suffix) {
    this.suffix = suffix;
    return this;
  }

  public int getOrder() {
    return order;
  }

  public AccessLogItemMeta<T> setOrder(int order) {
    this.order = order;
    return this;
  }

  public AccessLogItemCreator<T> getAccessLogItemCreator() {
    return accessLogItemCreator;
  }

  public AccessLogItemMeta<T> setAccessLogItemCreator(
      AccessLogItemCreator<T> accessLogItemCreator) {
    this.accessLogItemCreator = accessLogItemCreator;
    return this;
  }
}
