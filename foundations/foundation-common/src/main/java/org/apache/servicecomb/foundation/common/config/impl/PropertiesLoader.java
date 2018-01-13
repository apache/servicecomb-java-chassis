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

package org.apache.servicecomb.foundation.common.config.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.servicecomb.foundation.common.config.PaaSResourceUtils;
import org.springframework.core.io.Resource;

public class PropertiesLoader extends AbstractLoader {
  private List<Resource> foundResList = new ArrayList<>();

  public PropertiesLoader(List<String> locationPatternList) {
    super(locationPatternList);
  }

  public List<Resource> getFoundResList() {
    return foundResList;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T load() throws Exception {
    Properties props = new Properties();
    for (String locationPattern : locationPatternList) {
      List<Resource> resList = PaaSResourceUtils.getSortedPorperties(locationPattern);
      foundResList.addAll(resList);
      PaaSPropertiesLoaderUtils.fillAllProperties(props, resList);
    }

    return (T) props;
  }
}
