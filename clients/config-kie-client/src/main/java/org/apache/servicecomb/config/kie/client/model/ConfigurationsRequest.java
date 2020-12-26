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

package org.apache.servicecomb.config.kie.client.model;

public class ConfigurationsRequest {
  private String environment;

  private String application;

  private String serviceName;

  private String version;

  private String revision;

  public String getEnvironment() {
    return environment;
  }

  public ConfigurationsRequest setEnvironment(String environment) {
    this.environment = environment;
    return this;
  }

  public String getApplication() {
    return application;
  }

  public ConfigurationsRequest setApplication(String application) {
    this.application = application;
    return this;
  }

  public String getServiceName() {
    return serviceName;
  }

  public ConfigurationsRequest setServiceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  public String getVersion() {
    return version;
  }

  public ConfigurationsRequest setVersion(String version) {
    this.version = version;
    return this;
  }

  public String getRevision() {
    return revision;
  }

  public ConfigurationsRequest setRevision(String revision) {
    this.revision = revision;
    return this;
  }
}
