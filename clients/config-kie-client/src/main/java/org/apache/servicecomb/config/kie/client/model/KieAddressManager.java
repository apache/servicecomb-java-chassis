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

package org.apache.servicecomb.config.kie.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

public class KieAddressManager {

  private final Properties properties;

  private final List<String> addresses;

  private final Map<String, String> configKey;

  private int index;

  public KieAddressManager(Properties properties, List<String> addresses, Map<String, String> configKey) {
    this.properties = properties;
    this.addresses = new ArrayList<>(addresses.size());
    this.configKey = configKey;
    addresses.forEach((address -> this.addresses.add(address)));
    this.index = new Random().nextInt(addresses.size());
  }

  public String nextAddress() {
    synchronized (this) {
      this.index++;
      if (this.index >= addresses.size()) {
        this.index = 0;
      }
    }
    return address();
  }

  public String address() {
    synchronized (this) {
      return addresses.get(index);
    }
  }

  public Properties getProperties() {
    return properties;
  }

  public Map<String, String> getConfigKey() {
    return configKey;
  }
}
