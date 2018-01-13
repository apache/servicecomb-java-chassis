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

package org.apache.servicecomb.serviceregistry.api.registry;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by   on 2017/2/21.
 */
public enum WatchAction {
  CREATE("CREATE"),
  UPDATE("UPDATE"),
  // When SC send an EXPIRE action, which means client should clean up all local instance cache and fetch again.
  // This usually happens when SC adds new WHITE/BLACK rules or changes TAGS of instance
  EXPIRE("EXPIRE"),
  DELETE("DELETE");

  private String name;

  WatchAction(String name) {
    this.name = name;
  }

  @JsonValue
  public String getName() {
    return name;
  }
}
