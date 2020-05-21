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

import com.netflix.config.DynamicPropertyFactory;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;

import org.apache.servicecomb.zeroconfig.client.ClientUtil;
import org.apache.servicecomb.zeroconfig.client.ZeroConfigRegistryClientImpl;
import org.apache.servicecomb.zeroconfig.server.ServerUtil;
import org.apache.servicecomb.zeroconfig.server.ZeroConfigRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.MulticastSocket;
import java.util.function.Function;

/**
 * ZeroConfigServiceRegistryClientImpl is injected before cse application listener (order is -1000)
 */

@Configuration
@Order(-1001)
public class ZeroConfigRegistryApplicationListener implements ApplicationListener<ApplicationEvent>,
    ApplicationContextAware {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ZeroConfigRegistryApplicationListener.class);

  private MulticastSocket multicastSocket;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    // same mechanism as Local registry to enable the Zero Config registry
    boolean enable = DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.zeroconfig.enabled", false).get();

    if (enable) {
      ServerUtil.init();
      ClientUtil.init();

      try {
        this.multicastSocket = new MulticastSocket();
      } catch (IOException e) {
        LOGGER.error("Failed to create MulticastSocket object", e);
      }

      Function<ServiceRegistry, ServiceRegistryClient> registryClientConstructor =
          serviceRegistry -> new ZeroConfigRegistryClientImpl(new ZeroConfigRegistryService(),
              multicastSocket, new RestTemplate());

      ServiceRegistryConfig.INSTANCE.setServiceRegistryClientConstructor(registryClientConstructor);
    }

  }

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
  }
}
