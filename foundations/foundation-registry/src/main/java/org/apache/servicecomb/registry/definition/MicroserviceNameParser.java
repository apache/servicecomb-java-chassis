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
package org.apache.servicecomb.registry.definition;

/**
 * Parse application and microservice from join name.
 *
 * 1. microserviceName
 * 2. application:microserviceName
 */
public class MicroserviceNameParser {
  private String appId;

  private String microserviceName;

  public MicroserviceNameParser(String defaultAppId, String microserviceName) {
    parseMicroserviceName(defaultAppId, microserviceName);
  }

  private void parseMicroserviceName(String defaultAppId, String microserviceName) {
    int idxAt = microserviceName.indexOf(DefinitionConst.APP_SERVICE_SEPARATOR);
    if (idxAt == -1) {
      this.appId = defaultAppId;
      this.microserviceName = microserviceName;
      return;
    }

    this.appId = microserviceName.substring(0, idxAt);
    this.microserviceName = microserviceName.substring(idxAt + 1);
  }

  public String getAppId() {
    return appId;
  }

  public String getMicroserviceName() {
    return microserviceName;
  }
}
