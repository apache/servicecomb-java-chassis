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

package org.apache.servicecomb.qps;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;

/**
 * <p>
 * Use microservice.schema.operation as key.
 * If there is only configuration of microservice level, then all invocations are mapped to the qpsController of this configuration.
 * If there are configurations of schema and operation level, then the corresponding invocations is mapped to the qpsController
 * separately.
 * </p>
 *
 * <p>
 * When qpsController of schema level counts, the qpsController of microservice level won't count.
 * It's the same as operation level qpsController.
 * i.e. the statistics are only taken in qpsController internally. And between the qpsControllers there is no relationship.
 * </p>
 */
public class ConsumerQpsControllerManager extends AbstractQpsControllerManager {
  public ConsumerQpsControllerManager() {
    qpsDynamicConfigWatcher.setQpsLimitConfigKeyPrefix(Config.CONSUMER_LIMIT_KEY_PREFIX);
  }

  @Override
  protected String getKey(Invocation invocation) {
    return invocation.getOperationMeta().getMicroserviceQualifiedName();
  }

  @Override
  protected QpsController create(Invocation invocation) {
    // create is synchronized in parent class, there is no concurrent situation
    OperationMeta operationMeta = invocation.getOperationMeta();
    return qpsDynamicConfigWatcher.getOrCreateQpsController(operationMeta.getMicroserviceName(), operationMeta);
  }
}
