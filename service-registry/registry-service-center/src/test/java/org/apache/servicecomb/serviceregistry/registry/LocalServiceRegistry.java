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
package org.apache.servicecomb.serviceregistry.registry;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.serviceregistry.client.LocalServiceRegistryClientImpl;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;

import com.google.common.eventbus.EventBus;

public class LocalServiceRegistry extends AbstractServiceRegistry {
  private String localFile;

  public LocalServiceRegistry(EventBus eventBus, ServiceRegistryConfig serviceRegistryConfig,
      Configuration configuration) {
    super(eventBus, serviceRegistryConfig, configuration);
  }

  public LocalServiceRegistry localFile(String localFile) {
    this.localFile = localFile;
    return this;
  }

  protected ServiceRegistryClient createServiceRegistryClient() {
    return new LocalServiceRegistryClientImpl(localFile);
  }
}
