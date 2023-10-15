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
package org.apache.servicecomb.zeroconfig;

import java.io.IOException;

import org.apache.servicecomb.registry.lightweight.MessageExecutor;
import org.apache.servicecomb.zeroconfig.multicast.Multicast;
import org.apache.servicecomb.zeroconfig.multicast.MulticastServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZeroConfigRegistryConfiguration {
  @Bean
  public ZeroConfigRegistration zeroConfigRegistration() {
    return new ZeroConfigRegistration();
  }

  @Bean
  public MulticastServer multicastServer(Config config, Multicast multicast, MessageExecutor messageExecutor) {
    return new MulticastServer(config, multicast, messageExecutor);
  }

  @Bean
  public ZeroConfigDiscovery zeroConfigDiscovery() {
    return new ZeroConfigDiscovery();
  }

  @Bean
  public Multicast zeroConfigMulticast(Config config) throws IOException {
    return new Multicast(config);
  }

  @Bean
  public Config zeroConfigModel() {
    return new Config();
  }
}
