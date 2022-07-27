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

package org.apache.servicecomb.router;

import java.util.HashMap;
import java.util.Map;

public class ServiceIns {
  String version = "1.1";

  String serverName;

  Map<String, String> tags = new HashMap<>();

  private final String id;

  public ServiceIns(String id, String serverName) {
    this.id = id;
    this.serverName = serverName;
  }

  public String getId() {
    return id;
  }

  public String getVersion() {
    return version;
  }

  public String getServerName() {
    return serverName;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void addTags(String key, String v) {
    tags.put(key, v);
  }
}
