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

package org.apache.servicecomb.serviceregistry.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.serviceregistry.api.Const;
import org.apache.servicecomb.serviceregistry.api.registry.BasePath;
import org.junit.Assert;
import org.junit.Test;

public class TestConfigurePropertyUtils {
  @Test
  public void testGetPropertiesWithPrefix() {
    Configuration configuration = ConfigUtil.createLocalConfig();

    String prefix = "service_description.properties";
    Map<String, String> expectedMap = new HashMap<>();
    expectedMap.put("key1", "value1");
    expectedMap.put("key2", "value2");
    Assert.assertEquals(expectedMap, ConfigurePropertyUtils.getPropertiesWithPrefix(configuration, prefix));

    List<BasePath> paths = ConfigurePropertyUtils.getMicroservicePaths(configuration);
    Assert.assertEquals(2, paths.size());
    Assert.assertEquals(paths.get(0).getPath(), "/test1/testpath");
    Assert.assertEquals(paths.get(0).getProperty().get("checksession"), false);

    System.setProperty(Const.URL_PREFIX, "/webroot");
    paths = ConfigurePropertyUtils.getMicroservicePaths(configuration);
    Assert.assertEquals(2, paths.size());
    Assert.assertEquals(paths.get(0).getPath(), "/webroot/test1/testpath");
    Assert.assertEquals(paths.get(0).getProperty().get("checksession"), false);
  }
}
