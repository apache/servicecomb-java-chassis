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

package org.apache.servicecomb.metrics.overwatch.dto;

public class SystemFailure {
  private final Integer time;

  private final String system;

  private final String host;

  private final String url;

  private final String status;

  public Integer getTime() {
    return time;
  }

  public String getSystem() {
    return system;
  }

  public String getHost() {
    return host;
  }

  public String getUrl() {
    return url;
  }

  public String getStatus() {
    return status;
  }

  public SystemFailure(Integer time, String system, String host, String url, String status) {
    this.time = time;
    this.system = system;
    this.host = host;
    this.url = url;
    this.status = status;
  }
}
