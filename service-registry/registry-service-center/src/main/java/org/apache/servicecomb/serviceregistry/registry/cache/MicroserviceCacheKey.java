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

package org.apache.servicecomb.serviceregistry.registry.cache;

import java.util.Objects;

import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.registry.definition.MicroserviceNameParser;

public class MicroserviceCacheKey {
  private String env;

  private String appId;

  private String serviceName;

  private static final String VERSION_RULE = DefinitionConst.VERSION_RULE_ALL;

  public static MicroserviceCacheKeyBuilder builder() {
    return new MicroserviceCacheKeyBuilder();
  }

  MicroserviceCacheKey() {
  }

  public void validate() {
    Objects.requireNonNull(this.env, "microserviceCacheKey.env is null");
    Objects.requireNonNull(this.appId, "microserviceCacheKey.appId is null");
    Objects.requireNonNull(this.serviceName, "microserviceCacheKey.serviceName is null");
  }

  public String getEnv() {
    return env;
  }

  public String getAppId() {
    return appId;
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getVersionRule() {
    return VERSION_RULE;
  }

  public String plainKey() {
    return serviceName + "@" + appId + "@" + env;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MicroserviceCacheKey that = (MicroserviceCacheKey) o;
    return Objects.equals(env, that.env) &&
        Objects.equals(appId, that.appId) &&
        Objects.equals(serviceName, that.serviceName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(env, appId, serviceName);
  }

  @Override
  public String toString() {
    return plainKey();
  }

  public static class MicroserviceCacheKeyBuilder {
    private MicroserviceCacheKey microserviceCacheKey;

    public MicroserviceCacheKey build() {
      microserviceCacheKey.validate();
      MicroserviceNameParser microserviceNameParser =
          new MicroserviceNameParser(microserviceCacheKey.appId, microserviceCacheKey.serviceName);
      microserviceCacheKey.appId = microserviceNameParser.getAppId();
      microserviceCacheKey.serviceName = microserviceNameParser.getShortName();
      return microserviceCacheKey;
    }

    public MicroserviceCacheKeyBuilder env(String env) {
      microserviceCacheKey.env = env;
      return this;
    }

    public MicroserviceCacheKeyBuilder appId(String appId) {
      microserviceCacheKey.appId = appId;
      return this;
    }

    public MicroserviceCacheKeyBuilder serviceName(String serviceName) {
      microserviceCacheKey.serviceName = serviceName;
      return this;
    }

    MicroserviceCacheKeyBuilder() {
      microserviceCacheKey = new MicroserviceCacheKey();
    }
  }
}
