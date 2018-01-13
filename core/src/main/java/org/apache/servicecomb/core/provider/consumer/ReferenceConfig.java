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
import org.apache.servicecomb.core.definition.schema.ConsumerSchemaFactory;

public class ReferenceConfig {
  private MicroserviceMeta microserviceMeta;

  private String microserviceVersionRule = Const.DEFAULT_VERSION_RULE;

  private String transport = Const.ANY_TRANSPORT;

  public ReferenceConfig() {
  }

  public ReferenceConfig(ConsumerSchemaFactory consumerSchemaFactory, String microserviceName,
      String microserviceVersionRule, String transport) {
    this.microserviceMeta = consumerSchemaFactory.getOrCreateMicroserviceMeta(microserviceName,
        microserviceVersionRule);

    this.microserviceVersionRule = microserviceVersionRule;
    this.transport = transport;
  }

  public MicroserviceMeta getMicroserviceMeta() {
    return microserviceMeta;
  }

  public void setMicroserviceMeta(MicroserviceMeta microserviceMeta) {
    this.microserviceMeta = microserviceMeta;
  }

  public String getMicroserviceVersionRule() {
    return microserviceVersionRule;
  }

  public void setMicroserviceVersionRule(String microserviceVersionRule) {
    this.microserviceVersionRule = microserviceVersionRule;
  }

  public String getTransport() {
    return transport;
  }

  public void setTransport(String transport) {
    this.transport = transport;
  }
}
