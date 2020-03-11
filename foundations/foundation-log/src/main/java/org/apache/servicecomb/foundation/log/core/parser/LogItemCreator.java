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

import org.apache.servicecomb.foundation.log.core.element.LogItem;
import org.apache.servicecomb.foundation.log.core.element.impl.CookieItem;

/**
 * The {@linkplain LogItemCreator}s are able to instantiate a group of {@linkplain LogItem}.
 */
public interface LogItemCreator<T> {
  /**
   * Create an instance of {@linkplain LogItem} which is specified by the config.
   * @param config
   * e.g. For {@linkplain CookieItem CookieItem},
   * the pattern may be "%{varName}C", and it's config is "varName". Some {@linkplain LogItem} with no configurable
   * pattern (like "%m") will receive {@code null} as config.
   */
  LogItem<T> createItem(String config);
}
