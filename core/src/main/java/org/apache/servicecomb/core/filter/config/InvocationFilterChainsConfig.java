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
package org.apache.servicecomb.core.filter.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.config.ConfigUtil;
import org.springframework.util.CollectionUtils;

public class InvocationFilterChainsConfig extends AbstractFilterChainsConfig {
  private final String policiesRoot;

  private final List<String> frameworkChain;

  private final List<String> defaultChain;

  private final Map<String, List<String>> microserviceChains = new HashMap<>();

  public InvocationFilterChainsConfig(String root) {
    frameworkChain = ConfigUtil.getStringList(config, root + ".framework");
    defaultChain = loadDefaultChain(root);

    policiesRoot = root + ".policies";
    loadKeys(policiesRoot, this::loadPolicies);
  }

  private List<String> loadDefaultChain(String root) {
    String defaultChainKey = root + ".default";
    List<String> defaultChain = ConfigUtil.getStringList(config, defaultChainKey);
    if (CollectionUtils.isEmpty(defaultChain) && config.getProperty(defaultChainKey) == null) {
      defaultChain = frameworkChain;
    }
    return defaultChain;
  }

  private void loadPolicies(String qualifiedKey) {
    String microserviceName = qualifiedKey.substring(policiesRoot.length() + 1);

    microserviceChains.put(microserviceName, ConfigUtil.getStringList(config, qualifiedKey));
  }

  public List<String> getFrameworkChain() {
    return frameworkChain;
  }

  public List<String> getDefaultChain() {
    return defaultChain;
  }

  public Map<String, List<String>> getMicroserviceChains() {
    return microserviceChains;
  }
}
