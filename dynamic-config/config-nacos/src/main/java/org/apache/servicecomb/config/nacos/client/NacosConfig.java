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

package org.apache.servicecomb.config.nacos.client;

import org.apache.commons.configuration.Configuration;

public class NacosConfig {
  public static final NacosConfig INSTANCE = new NacosConfig();

  private static Configuration finalConfig;

  public static final String DATA_ID = "servicecomb.nacos.dataId";

  public static final String SERVER_ADDR = "servicecomb.nacos.serverAddr";

  public static final String GROUP = "servicecomb.nacos.group";

  public static final String ADD_PREFIX = "servicecomb.nacos.addPrefix";

  public static final String NAME_SPACE = "servicecomb.nacos.namespace";

  public static final String CONTENT_TYPE = "servicecomb.nacos.contentType";

  private NacosConfig() {
  }

  public static void setConcurrentCompositeConfiguration(Configuration config) {
    finalConfig = config;
  }

  public String getServerAddr() {
    return finalConfig.getString(SERVER_ADDR);
  }

  public String getDataId() {
    return finalConfig.getString(DATA_ID);
  }

  public String getGroup() {
    return finalConfig.getString(GROUP);
  }

  public String getNameSpace() {
    return finalConfig.getString(NAME_SPACE, "public");
  }

  public String getContentType() {
    return finalConfig.getString(CONTENT_TYPE, "yaml");
  }

  public boolean getAddPrefix() {
    return finalConfig.getBoolean(ADD_PREFIX, true);
  }
}
