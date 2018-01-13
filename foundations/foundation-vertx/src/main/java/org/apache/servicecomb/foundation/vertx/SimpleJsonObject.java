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

package org.apache.servicecomb.foundation.vertx;

import io.vertx.core.json.JsonObject;

public class SimpleJsonObject extends JsonObject {
  /**
   * 直接保存进map，规避原来的put不支持Object的问题
   */
  @Override
  public JsonObject put(String key, Object value) {
    getMap().put(key, value);
    return this;
  }

  /**
   * 不必复制，直接使用，规避原来的copy不支持Object的问题
   */
  @Override
  public JsonObject copy() {
    return this;
  }
}
