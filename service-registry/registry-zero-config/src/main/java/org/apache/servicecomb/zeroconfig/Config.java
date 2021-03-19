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

import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.CFG_ADDRESS;
import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.CFG_ENABLED;
import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.CFG_GROUP;
import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.DEFAULT_ADDRESS;
import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.DEFAULT_GROUP;

import org.apache.servicecomb.config.DynamicProperties;
import org.springframework.stereotype.Component;

@Component
public class Config {
  private final DynamicProperties dynamicProperties;

  public Config(DynamicProperties dynamicProperties) {
    this.dynamicProperties = dynamicProperties;
  }

  public boolean isEnabled() {
    return dynamicProperties.getBooleanProperty(CFG_ENABLED, true);
  }

  public String getAddress() {
    return dynamicProperties.getStringProperty(CFG_ADDRESS, DEFAULT_ADDRESS);
  }

  // (224.0.0.0, 239.255.255.255]
  public String getGroup() {
    return dynamicProperties.getStringProperty(CFG_GROUP, DEFAULT_GROUP);
  }
}
