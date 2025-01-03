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

package org.apache.servicecomb.config.consul;

public class ConsulConfigProperties {

  private String host = "localhost";

  private Integer port = 8500;

  private String scheme = "http";

  private String aclToken;

  private Integer watchSeconds = 8;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public String getScheme() {
    return scheme;
  }

  public void setScheme(String scheme) {
    this.scheme = scheme;
  }

  public String getAclToken() {
    return aclToken;
  }

  public void setAclToken(String aclToken) {
    this.aclToken = aclToken;
  }

  public Integer getWatchSeconds() {
    return watchSeconds;
  }

  public void setWatchSeconds(Integer watchSeconds) {
    this.watchSeconds = watchSeconds;
  }
}
