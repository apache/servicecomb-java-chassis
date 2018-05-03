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

package org.apache.servicecomb.config.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.foundation.common.event.EventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import io.vertx.core.json.JsonObject;

/**
 * Created by on 2016/11/19.
 */
public class MemberDiscovery {

  private static final Logger LOGGER = LoggerFactory.getLogger(MemberDiscovery.class);

  private static final String SCHEMA_SEPRATOR = "://";

  private List<String> configServerAddresses = new ArrayList<>();

  private Object lock = new Object();

  private AtomicInteger counter = new AtomicInteger(0);

  public MemberDiscovery(List<String> configCenterUri) {
    if (configCenterUri != null && !configCenterUri.isEmpty()) {
      configServerAddresses.addAll(configCenterUri);
    }
    Collections.shuffle(configServerAddresses);
    EventManager.register(this);
  }

  public String getConfigServer() {
    synchronized (lock) {
      if (configServerAddresses.isEmpty()) {
        throw new IllegalStateException("Config center address is not available.");
      }
      int index = Math.abs(counter.get() % configServerAddresses.size());
      return configServerAddresses.get(index);
    }
  }

  @Subscribe
  public void onConnFailEvent(ConnFailEvent e) {
    counter.incrementAndGet();
  }

  public void refreshMembers(JsonObject members) {
    List<String> newServerAddresses = new ArrayList<>();
    members.getJsonArray("instances").forEach(m -> {
      JsonObject instance = (JsonObject) m;
      if ("UP".equals(instance.getString("status", "UP"))) {
        String endpoint = instance.getJsonArray("endpoints").getString(0);
        String scheme = instance.getBoolean("isHttps", false) ? "https" : "http";
        newServerAddresses.add(scheme + SCHEMA_SEPRATOR
            + endpoint.substring(endpoint.indexOf(SCHEMA_SEPRATOR) + SCHEMA_SEPRATOR.length()));
      }
    });

    synchronized (lock) {
      this.configServerAddresses.clear();
      this.configServerAddresses.addAll(newServerAddresses);
      Collections.shuffle(this.configServerAddresses);
    }
    LOGGER.info("New config center members: {}", this.configServerAddresses);
  }
}
