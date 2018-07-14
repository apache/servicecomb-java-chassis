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

package org.apache.servicecomb.config.archaius.sources;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.config.YAMLUtil;

import com.netflix.config.PollResult;
import com.netflix.config.PolledConfigurationSource;

public class MicroserviceConfigurationSource implements PolledConfigurationSource {
  private final List<ConfigModel> configModels;

  public MicroserviceConfigurationSource(List<ConfigModel> configModels) {
    this.configModels = configModels;
  }

  public List<ConfigModel> getConfigModels() {
    return configModels;
  }

  public PollResult poll(boolean b, Object o) throws Exception {
    Map<String, Object> configurations = new LinkedHashMap<>();

    for (ConfigModel configModel : configModels) {
      configurations.putAll(YAMLUtil.retrieveItems("", configModel.getConfig()));
    }

    return PollResult.createFull(configurations);
  }
}
