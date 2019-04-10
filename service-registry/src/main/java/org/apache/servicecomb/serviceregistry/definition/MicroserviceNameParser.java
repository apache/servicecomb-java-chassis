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
package org.apache.servicecomb.serviceregistry.definition;

import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.Const;

public class MicroserviceNameParser {
  private String appId;

  // always not include appId
  private String shortName;

  // inside app: equals to shortName
  // cross app: appId:shortName
  private String microserviceName;

  public MicroserviceNameParser(String microserviceName) {
    parseMicroserviceName(microserviceName);
  }

  private void parseMicroserviceName(String microserviceName) {
    this.microserviceName = microserviceName;

    int idxAt = microserviceName.indexOf(Const.APP_SERVICE_SEPARATOR);
    if (idxAt == -1) {
      this.appId = RegistryUtils.getAppId();
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
