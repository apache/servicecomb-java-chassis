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

import org.apache.servicecomb.core.definition.ConsumerMicroserviceVersionsMeta;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;

/**
 * microservice meta data for consumer.
 */
public class MicroserviceReferenceConfig {
  private final String appId;

  private final String microserviceName;

  private final ConsumerMicroserviceVersionsMeta microserviceVersionsMeta;

  private final MicroserviceMeta microserviceMeta;

  public MicroserviceReferenceConfig(
      String appId,
      String microserviceName,
      ConsumerMicroserviceVersionsMeta microserviceVersionsMeta,
      MicroserviceMeta microserviceMeta) {
    this.appId = appId;
    this.microserviceName = microserviceName;
    this.microserviceVersionsMeta = microserviceVersionsMeta;
    this.microserviceMeta = microserviceMeta;
  }

  public MicroserviceMeta getMicroserviceMeta() {
    if (microserviceMeta == null) {
      throw new IllegalStateException(
          String.format(
              "Probably invoke a service before it is registered, or no instance found for it, appId=%s, name=%s.",
              appId,
              microserviceName));
    }

    return microserviceMeta;
  }

  public ReferenceConfig createReferenceConfig(OperationMeta operationMeta) {
    return createReferenceConfig(null, operationMeta);
  }

  public ReferenceConfig createReferenceConfig(String transport, OperationMeta operationMeta) {
    if (transport == null) {
      transport = operationMeta.getConfig().getTransport();
    }
    final ReferenceConfig referenceConfig = new ReferenceConfig(transport);
    return referenceConfig;
  }
}
