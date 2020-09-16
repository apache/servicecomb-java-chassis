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

package org.apache.servicecomb.config.center.client;

import org.apache.servicecomb.config.center.client.exception.OperationException;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsRequest;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsResponse;

public interface ConfigCenterOperation {
  /**
   * 根据查询条件查询配置项。
   * @param request 查询的维度(project, application, serviceName, version) 和 revision 信息。
   * @return 如果存在配置变更，返回全量的配置项, changed = true。 如果没有变更， 返回 null, changed = false，
   *  @throws OperationException If some problems happened to contact service center or non http 200 returned.
   */
  QueryConfigurationsResponse queryConfigurations(QueryConfigurationsRequest request);
}
