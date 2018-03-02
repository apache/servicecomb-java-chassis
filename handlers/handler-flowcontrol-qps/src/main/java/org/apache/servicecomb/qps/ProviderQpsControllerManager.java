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

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.qps.config.QpsDynamicConfigWatcher;
import org.springframework.util.StringUtils;

public class ProviderQpsControllerManager extends AbstractQpsControllerManager {
  public ProviderQpsControllerManager() {
    qpsDynamicConfigWatcher.setQpsLimitConfigKeyPrefix(Config.PROVIDER_LIMIT_KEY_PREFIX);
    qpsDynamicConfigWatcher.setGlobalQpsController(Config.PROVIDER_LIMIT_KEY_GLOBAL);
  }

  @Override
  public QpsController getOrCreate(Invocation invocation) {
    if (StringUtils.isEmpty(getConsumerMicroserviceName(invocation))) {
      return qpsDynamicConfigWatcher.getGlobalQpsController();
    }

    return super.getOrCreate(invocation);
  }

  @Override
  protected String getKey(Invocation invocation) {
    String microServiceName = getConsumerMicroserviceName(invocation);
    return microServiceName + QpsDynamicConfigWatcher.SEPARATOR
        + invocation.getOperationMeta().getSchemaQualifiedName();
  }

  @Override
  protected QpsController create(Invocation invocation) {
    // create is synchronized in parent class, there is no concurrent situation
    String microServiceName = getConsumerMicroserviceName(invocation);
    return qpsDynamicConfigWatcher.getOrCreateQpsController(microServiceName, invocation.getOperationMeta());
  }

  private String getConsumerMicroserviceName(Invocation invocation) {
    return (String) invocation.getContext(Const.SRC_MICROSERVICE);
  }
}
