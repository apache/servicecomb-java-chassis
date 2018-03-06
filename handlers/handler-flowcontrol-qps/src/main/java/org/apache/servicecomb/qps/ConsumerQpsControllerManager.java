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

import com.netflix.config.DynamicProperty;

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
  private static volatile ConsumerQpsControllerManager INSTANCE;

  private ConsumerQpsControllerManager() {
  }

  public static ConsumerQpsControllerManager getINSTANCE() {
    if (null == INSTANCE) {
      synchronized (ConsumerQpsControllerManager.class) {
        if (null == INSTANCE) {
          INSTANCE = new ConsumerQpsControllerManager();
        }
      }
    }
    return INSTANCE;
  }

  @Override
  protected DynamicProperty getDynamicProperty(String configKey) {
    return DynamicProperty.getInstance(Config.CONSUMER_LIMIT_KEY_PREFIX + configKey);
  }
}
