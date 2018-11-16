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
package org.apache.servicecomb.springboot.starter.registry;

import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

/**
 * Initialize and Register the services with service center
 */
public class RegistryIntializer {
  private static final Logger LOG = LoggerFactory.getLogger(RegistryIntializer.class);

  private RegistryIntializer() {

  }

  public static void initRegistry() {
    String address = DynamicPropertyFactory.getInstance().getStringProperty("servicecomb.rest.address", null).get();
    if (null != address) {
      try {
        RegistryUtils.init();
        RegistryUtils.getMicroserviceInstance().getEndpoints().add(RegistryUtils.getPublishAddress("rest", address));
        RegistryUtils.run();
      } catch (Exception e) {
        LOG.error("init registry error.", e);
      }
    } else {
      LOG.info("rest address is null.Service is not registered to service center");
    }
  }
}
