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

package org.apache.servicecomb.config.nacos;

import org.springframework.core.env.Environment;

public class NacosConfig {
  public static final String PROPERTY_DATA_ID = "servicecomb.nacos.dataId";

  public static final String PROPERTY_SERVER_ADDR = "servicecomb.nacos.serverAddr";

  public static final String PROPERTY_GROUP = "servicecomb.nacos.group";

  public static final String PROPERTY_ADD_PREFIX = "servicecomb.nacos.addPrefix";

  public static final String PROPERTY_CONTENT_TYPE = "servicecomb.nacos.contentType";

  public static final String PROPERTY_USERNAME = "servicecomb.nacos.username";

  public static final String PROPERTY_PASSWORD = "servicecomb.nacos.password";

  public static final String PROPERTY_ACCESS_KEY = "servicecomb.nacos.accessKey";

  public static final String PROPERTY_SECRET_KEY = "servicecomb.nacos.secretKey";

  public static final String PROP_NAMESPACE = "namespace";

  public static final String PROP_ADDRESS = "serverAddr";

  public static final String PROP_USERNAME = "username";

  public static final String PROP_PASSWORD = "password";

  public static final String PROP_ACCESS_KEY = "accessKey";

  public static final String PROP_SECRET_KEY = "secretKey";

  private final Environment environment;

  public NacosConfig(Environment environment) {
    this.environment = environment;
  }

  public String getServerAddr() {
    return environment.getProperty(PROPERTY_SERVER_ADDR, "http://127.0.0.1:8848");
  }

  public String getDataId() {
    return environment.getProperty(PROPERTY_DATA_ID);
  }

  public String getGroup() {
    return environment.getProperty(PROPERTY_GROUP);
  }

  public String getUsername() {
    return environment.getProperty(PROPERTY_USERNAME);
  }

  public String getPassword() {
    return environment.getProperty(PROPERTY_PASSWORD);
  }

  public String getAccessKey() {
    return environment.getProperty(PROPERTY_ACCESS_KEY);
  }

  public String getSecretKey() {
    return environment.getProperty(PROPERTY_SECRET_KEY);
  }

  public String getContentType() {
    return environment.getProperty(PROPERTY_CONTENT_TYPE, "yaml");
  }

  public boolean getAddPrefix() {
    return environment.getProperty(PROPERTY_ADD_PREFIX, boolean.class, false);
  }
}
