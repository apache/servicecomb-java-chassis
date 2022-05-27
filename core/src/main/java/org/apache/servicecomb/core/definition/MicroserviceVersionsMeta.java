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
package org.apache.servicecomb.core.definition;

import java.util.Map;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

public class MicroserviceVersionsMeta {
  protected final SCBEngine scbEngine;

  protected final String microserviceName;

  // key is operationMeta.getMicroserviceQualifiedName()
  private final Map<String, OperationConfig> configs = new ConcurrentHashMapEx<>();

  protected volatile MicroserviceConfig microserviceConfig;

  public MicroserviceVersionsMeta(SCBEngine scbEngine, String microserviceName) {
    this.scbEngine = scbEngine;
    this.microserviceName = microserviceName;
  }

  // should not create in constructor
  // when invoke not exist microservice, will create DynamicProperty(com.netflix.config.DynamicProperty.getInstance)
  // edge scene, if attacker request to forward request to not exist microservice, will cause OOM
  public MicroserviceConfig getMicroserviceConfig() {
    if (microserviceConfig == null) {
      synchronized (this) {
        if (microserviceConfig == null) {
          this.microserviceConfig = scbEngine.getPriorityPropertyManager()
              .createConfigObject(MicroserviceConfig.class, "service", microserviceName);
        }
      }
    }

    return microserviceConfig;
  }

  public OperationConfig getOrCreateOperationConfig(OperationMeta operationMeta) {
    return configs.computeIfAbsent(operationMeta.getMicroserviceQualifiedName(),
        name -> createOperationConfig(operationMeta));
  }

  private OperationConfig createOperationConfig(OperationMeta operationMeta) {
    boolean consumer = operationMeta.getMicroserviceMeta().isConsumer();
    return scbEngine.getPriorityPropertyManager().createConfigObject(
        OperationConfig.class,
        "op-any-priority", consumer ?
            OperationConfig.CONSUMER_OP_ANY_PRIORITY : OperationConfig.PRODUCER_OP_ANY_PRIORITY,
        "consumer-op-any_priority", OperationConfig.CONSUMER_OP_ANY_PRIORITY,
        "producer-op-any_priority", OperationConfig.PRODUCER_OP_ANY_PRIORITY,

        "op-priority", consumer ?
            OperationConfig.CONSUMER_OP_PRIORITY : OperationConfig.PRODUCER_OP_PRIORITY,
        "consumer-op-priority", OperationConfig.CONSUMER_OP_PRIORITY,
        "producer-op-priority", OperationConfig.PRODUCER_OP_PRIORITY,

        "consumer-producer", consumer ? "Consumer" : "Provider",
        "consumer-provider", consumer ? "Consumer" : "Provider",

        "service", operationMeta.getMicroserviceName(),
        "schema", operationMeta.getSchemaId(),
        "operation", operationMeta.getOperationId());
  }
}
