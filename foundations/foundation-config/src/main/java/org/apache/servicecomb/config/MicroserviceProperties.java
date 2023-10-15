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
package org.apache.servicecomb.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class MicroserviceProperties {
  public static final String PREFIX = "servicecomb.service";

  private String environment;

  private String application;

  private String name;

  private String alias;

  private String version;

  private String description;

  private Map<String, String> properties = new HashMap<>();

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public String getApplication() {
    if (StringUtils.isEmpty(application)) {
      throw new IllegalStateException(
          "Application Name is required in configuration. NOTICE: since 3.0.0, only support "
              + PREFIX + ".application to configure microservice application.");
    }
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public String getName() {
    if (StringUtils.isEmpty(name)) {
      throw new IllegalStateException(
          "Service Name is required in configuration. NOTICE: since 3.0.0, only support "
              + PREFIX + ".name to configure microservice name.");
    }
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public String getVersion() {
    if (StringUtils.isEmpty(version)) {
      throw new IllegalStateException(
          "Service version is required in configuration. NOTICE: since 3.0.0, only support "
              + PREFIX + ".version to configure microservice version.");
    }
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
}
