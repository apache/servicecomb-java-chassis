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

package org.apache.servicecomb.demo.multiServiceCenterClient;

import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerBServiceCenterConfiguration {
  @Bean("serverBServiceCenterConfig")
  public ServiceRegistryConfig serverBServiceCenterConfig() {
    ServiceRegistryConfig config = ServiceRegistryConfig.buildFromConfiguration();
    return ServiceRegistryConfigCustomizer.from(config)
        .addressListFromConfiguration("servicecomb.service.registry-serverB.address")
        // use a different http client instance
        .setClientName("registry-serverB")
        .setWatchClientName("registry-watch-serverB")
        .setRegistryName("server-B")
        .get();
  }
}
