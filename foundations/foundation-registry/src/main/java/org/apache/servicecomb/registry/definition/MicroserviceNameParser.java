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
 * <pre>
 *   1. if microserviceName format is app:name, then will ignore defaultAppId
 *     appId = app
 *     shortName = name
 *     microserviceName = app:name
 *   2. if microserviceName format is name
 *     appId = defaultAppId
 *     shortName = name
 *     microserviceName = name
 * </pre>
 */
public class MicroserviceNameParser {
  private String appId;

  // always not include appId
  private String shortName;

  // inside app: equals to shortName
  // cross app: appId:shortName
  private String microserviceName;

  public MicroserviceNameParser(String defaultAppId, String microserviceName) {
    parseMicroserviceName(defaultAppId, microserviceName);
  }

  private void parseMicroserviceName(String defaultAppId, String microserviceName) {
    this.microserviceName = microserviceName;

    int idxAt = microserviceName.indexOf(DefinitionConst.APP_SERVICE_SEPARATOR);
    if (idxAt == -1) {
      this.appId = defaultAppId;
      this.shortName = microserviceName;
      return;
    }

    this.appId = microserviceName.substring(0, idxAt);
    this.shortName = microserviceName.substring(idxAt + 1);
  }

  public String getAppId() {
    return appId;
  }

  public String getShortName() {
    return shortName;
  }

  public String getMicroserviceName() {
    return microserviceName;
  }
}
