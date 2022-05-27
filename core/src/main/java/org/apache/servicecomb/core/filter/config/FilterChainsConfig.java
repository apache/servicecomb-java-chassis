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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.servicecomb.config.ConfigUtil;

public class FilterChainsConfig extends AbstractFilterChainsConfig {
  public static final String TRANSPORT_ROOT = ROOT + "transport";

  public static final String DEFINITION_ROOT = ROOT + "definition";

  // key is chain name
  private final Map<String, TransportChainsConfig> transportChains = new HashMap<>();

  private final Map<String, List<String>> definitions = new HashMap<>();

  private InvocationFilterChainsConfig consumer;

  private InvocationFilterChainsConfig producer;

  private boolean enabled;

  public void load() {
    enabled = config.getBoolean(ROOT + "enabled", false);

    loadKeys(TRANSPORT_ROOT, this::loadTransportChain);
    loadKeys(DEFINITION_ROOT, this::loadDefinitionChain);

    consumer = new InvocationFilterChainsConfig(ROOT + "consumer");
    producer = new InvocationFilterChainsConfig(ROOT + "producer");
  }

  private void loadTransportChain(String qualifiedKey) {
    String qualifiedName = qualifiedKey.substring(TRANSPORT_ROOT.length() + 1);
    int dotIdx = qualifiedName.indexOf('.');
    String chainName = qualifiedName.substring(0, dotIdx);
    String transport = qualifiedName.substring(dotIdx + 1);

    transportChains.computeIfAbsent(chainName, key -> new TransportChainsConfig())
        .add(transport, ConfigUtil.getStringList(config, qualifiedKey));
  }

  private void loadDefinitionChain(String qualifiedKey) {
    String chainName = qualifiedKey.substring(DEFINITION_ROOT.length() + 1);

    definitions.put(chainName, ConfigUtil.getStringList(config, qualifiedKey));
  }

  public boolean isEnabled() {
    return enabled;
  }

  public InvocationFilterChainsConfig getConsumer() {
    return consumer;
  }

  public InvocationFilterChainsConfig getProducer() {
    return producer;
  }

  public Function<List<String>, List<Object>> getResolver() {
    return this::resolveChain;
  }

  private List<Object> resolveChain(List<String> chain) {
    return chain.stream()
        .flatMap(filterOrReference -> resolveFilterOrReference(filterOrReference).stream())
        .collect(Collectors.toList());
  }

  private List<Object> resolveFilterOrReference(String filterOrReference) {
    TransportChainsConfig transportChain = transportChains.get(filterOrReference);
    if (transportChain != null) {
      return Collections.singletonList(transportChain);
    }

    List<String> chain = definitions.get(filterOrReference);
    if (chain == null) {
      return Collections.singletonList(filterOrReference);
    }

    return resolveChain(chain);
  }
}
