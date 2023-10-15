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

package org.apache.servicecomb.registry.lightweight.store;

import java.util.List;
import java.util.Objects;

import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.lightweight.model.MicroserviceInstance;

import com.google.common.base.Ticker;

public class InstanceStore {
  private final Ticker ticker;

  private MicroserviceInstance instance;

  private long lastHeartBeat;

  public InstanceStore(Ticker ticker, MicroserviceInstance instance) {
    this.ticker = ticker;
    this.instance = instance;

    this.updateLastHeartBeat();
  }

  public boolean isStatusChanged(MicroserviceInstanceStatus status) {
    return !Objects.equals(instance.getStatus(), status);
  }

  public MicroserviceInstance getInstance() {
    return instance;
  }

  public InstanceStore setInstance(MicroserviceInstance instance) {
    this.instance = instance;
    return this;
  }

  public String getInstanceId() {
    return getInstance().getInstanceId();
  }

  public String getServiceId() {
    return getInstance().getServiceId();
  }

  public List<String> getEndpoints() {
    return getInstance().getEndpoints();
  }

  public MicroserviceInstanceStatus getStatus() {
    return instance.getStatus();
  }

  public void setStatus(MicroserviceInstanceStatus status) {
    instance.setStatus(status);
  }

  public void updateLastHeartBeat() {
    this.lastHeartBeat = ticker.read();
  }

  public long getLastHeartBeat() {
    return lastHeartBeat;
  }

  public boolean isHeartBeatTimeout(long nanoNow, long nanoTimeout) {
    return (nanoNow - lastHeartBeat) > nanoTimeout;
  }
}
