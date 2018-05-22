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

package org.apache.servicecomb.core.provider.consumer;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.MicroserviceVersionMeta;
import org.apache.servicecomb.serviceregistry.consumer.AppManager;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersion;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersionRule;

public class ReferenceConfig {
  private MicroserviceVersionRule microserviceVersionRule;

  private String transport = Const.ANY_TRANSPORT;

  public ReferenceConfig() {
  }

  public ReferenceConfig(AppManager appManager, String microserviceName, String versionRule, String transport) {
    String appId = new MicroserviceMeta(microserviceName).getAppId();
    this.microserviceVersionRule = appManager.getOrCreateMicroserviceVersionRule(appId,
        microserviceName,
        versionRule);

    this.transport = transport;
  }

  public MicroserviceMeta getMicroserviceMeta() {
    MicroserviceVersion microserviceVersion = microserviceVersionRule.getLatestMicroserviceVersion();
    if (microserviceVersion == null) {
      throw new IllegalStateException(
          String.format(
              "Probably invoke a service before it is registered, or no instance found for it, appId=%s, name=%s",
              microserviceVersionRule.getAppId(),
              microserviceVersionRule.getMicroserviceName()));
    }

    return ((MicroserviceVersionMeta) microserviceVersion).getMicroserviceMeta();
  }

  public MicroserviceVersionRule getMicroserviceVersionRule() {
    return microserviceVersionRule;
  }

  public String getVersionRule() {
    return microserviceVersionRule.getVersionRule().getVersionRule();
  }

  public void setMicroserviceVersionRule(MicroserviceVersionRule microserviceVersionRule) {
    this.microserviceVersionRule = microserviceVersionRule;
  }

  public String getTransport() {
    return transport;
  }

  public void setTransport(String transport) {
    this.transport = transport;
  }
}
