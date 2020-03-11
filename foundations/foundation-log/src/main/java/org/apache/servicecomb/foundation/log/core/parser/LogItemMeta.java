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

package org.apache.servicecomb.foundation.log.core.parser;


public class LogItemMeta<T> {
  protected String prefix;

  protected String suffix;

  /**
   * Used for sorting {@linkplain LogItemMeta}. Default value is 0.
   * Smaller one has higher priority.
   */
  protected int order;

  protected LogItemCreator<T> logItemCreator;

  public String getPrefix() {
    return prefix;
  }

  public LogItemMeta<T> setPrefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

  public String getSuffix() {
    return suffix;
  }

  public LogItemMeta<T> setSuffix(String suffix) {
    this.suffix = suffix;
    return this;
  }

  public int getOrder() {
    return order;
  }

  public LogItemMeta<T> setOrder(int order) {
    this.order = order;
    return this;
  }

  public LogItemCreator<T> getLogItemCreator() {
    return logItemCreator;
  }

  public LogItemMeta<T> setLogItemCreator(LogItemCreator<T> logItemCreator) {
    this.logItemCreator = logItemCreator;
    return this;
  }
}
