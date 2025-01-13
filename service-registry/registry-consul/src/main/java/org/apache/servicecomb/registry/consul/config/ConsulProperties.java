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

package org.apache.servicecomb.registry.consul.config;

import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;


@Validated
public class ConsulProperties {

  @NotNull
  private String host = "localhost";

  private String scheme = "http";

  @NotNull
  private Integer port = 8500;

  private Boolean enabled = true;

  public String getHost() {
    return this.host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Integer getPort() {
    return this.port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public Boolean isEnabled() {
    return this.enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public String getScheme() {
    return this.scheme;
  }

  public void setScheme(String scheme) {
    this.scheme = scheme;
  }

  @Override
  public String toString() {
    return "ConsulProperties{" + "host='" + this.host + '\'' + ", port=" + this.port + ", scheme=" + this.scheme
        + ", enabled=" + this.enabled + "}";
  }
}
