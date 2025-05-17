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

package org.apache.servicecomb.config.kie.client;

import org.apache.servicecomb.config.common.exception.OperationException;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsRequest;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsResponse;

/**
 * Support configuration center extension
 */
public interface KieConfigOperation {
  /**
   * queries configuration items based on search criteria。
   * @param request query dimension(project, application, serviceName, version) and revision info。
   * @param address config center address。
   * @param isFirstPull is first pull configurations
   * @return if configuration changes, return all config items, changed = true。otherwise return null, changed = false，
   * @throws OperationException If some problems happened to contact service center or non http 200 returned.
   */
  ConfigurationsResponse queryConfigurations(ConfigurationsRequest request, String address, boolean isFirstPull);

  /**
   * Check kie isolation address available
   *
   * @param address isolation address
   */
  void checkAddressAvailable(String address);
}
