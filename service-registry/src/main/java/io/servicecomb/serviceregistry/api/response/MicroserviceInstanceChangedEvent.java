/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.serviceregistry.api.response;

import io.servicecomb.serviceregistry.api.MicroserviceKey;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.api.registry.WatchAction;

/**
 * Created by   on 2016/12/25.
 */
public class MicroserviceInstanceChangedEvent {
  private WatchAction action;

  private MicroserviceKey key;

  private MicroserviceInstance instance;

  public MicroserviceInstance getInstance() {
    return instance;
  }

  public void setInstance(MicroserviceInstance instance) {
    this.instance = instance;
  }

  public MicroserviceKey getKey() {
    return key;
  }

  public void setKey(MicroserviceKey key) {
    this.key = key;
  }

  public WatchAction getAction() {
    return action;
  }

  public void setAction(WatchAction action) {
    this.action = action;
  }
}
