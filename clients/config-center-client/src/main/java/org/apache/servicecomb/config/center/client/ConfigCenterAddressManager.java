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

import java.util.List;

import org.apache.servicecomb.http.client.common.AbstractAddressManager;
import org.apache.servicecomb.http.client.common.URLEndPoint;
import org.apache.servicecomb.http.client.event.RefreshEndpointEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ConfigCenterAddressManager extends AbstractAddressManager {

  public ConfigCenterAddressManager(String projectName, List<String> addresses, EventBus eventBus) {
    super(projectName, addresses);
    eventBus.register(this);
  }

  @Override
  protected String normalizeUri(String endpoint) {
    String address = new URLEndPoint(endpoint).toString();
    return formatAddress(address);
  }

  @Subscribe
  public void onRefreshEndpointEvent(RefreshEndpointEvent event) {
    refreshEndpoint(event, RefreshEndpointEvent.CONFIG_CENTER_NAME);
  }
}
